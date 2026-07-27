/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.TypeHierarchyUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * CDI diagnostics participant that validates decorator delegate injection points.
 *
 * Validates:
 * 1. A decorator must declare exactly one injection point annotated with @Delegate
 * 2. The delegate type must implement or extend all decorated types of the decorator
 * with exactly the same type parameters
 *
 * @see https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#delegate_attribute
 * @see https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#decorator_bean
 */
public class CdiDecoratorDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /**
     * Holds a decorated type's fully qualified name together with its raw
     * (erased) type signature and its type-argument signatures as declared on
     * the decorator's {@code implements} clause.
     */
    private static final class DecoratedTypeInfo {
        /** Fully qualified name of the raw (erased) type, e.g. {@code "com.example.Processor"} */
        final String fqName;
        /**
         * Type-argument signatures from the decorator's {@code implements} clause,
         * e.g. {@code ["Ljava.lang.String;"]} for {@code implements Processor<String>}.
         * Empty array when the decorated type is not parameterized.
         */
        final String[] typeArgs;

        DecoratedTypeInfo(String fqName, String[] typeArgs) {
            this.fqName = fqName;
            this.typeArgs = typeArgs;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(CdiDecoratorDiagnosticsParticipant.class.getName());

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        try {
            IType[] types = unit.getAllTypes();
            for (IType type : types) {
                validateDelegateUsage(type, unit, uri, context, diagnostics);
            }
        } catch (JavaModelException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while validating decorator", e);
        }

        return diagnostics;
    }

    /**
     * Single-pass validation of @Delegate usage for a type.
     *
     * For a class annotated with @Decorator: collects all @Delegate injection points
     * in one traversal and validates count, @Inject presence, and type assignability.
     *
     * For a class NOT annotated with @Decorator: any @Delegate injection point found
     * during the same traversal is immediately reported as a definition error.
     *
     * @param type the type to validate
     * @param unit the compilation unit
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateDelegateUsage(IType type, ICompilationUnit unit, String uri,
                                       JavaDiagnosticsContext context, List<Diagnostic> diagnostics) throws JavaModelException {

        boolean isDecorator = DiagnosticUtils.isMatchedAnnotation(unit, type.getAnnotations(), Constants.DECORATOR_FQ_NAME);

        List<IJavaElement> delegateElements = new ArrayList<>();
        for (IField field : type.getFields()) {
            validateDelegate(type, field, field, uri, context, diagnostics, delegateElements, isDecorator);
        }
        for (IMethod method : type.getMethods()) {
            IAnnotation[] methodAnnotations = method.getAnnotations();
            for (ILocalVariable parameter : method.getParameters()) {
                validateDelegate(type, method, parameter, uri, context, diagnostics, delegateElements, isDecorator, methodAnnotations);
            }
        }

        if (isDecorator) {
            reportInvalidDelegateCountDiagnostics(type, uri, context, diagnostics,
                                                  delegateElements, delegateElements.size());
            if (delegateElements.size() == 1) {
                validateDelegateTypeAssignability(type, delegateElements.get(0), uri, context, diagnostics);
            }
        }
    }

    /**
     * Unified delegate processing for fields and parameters.
     *
     * If {@code isDecorator} is true, collects the element into {@code delegateElements}
     * and validates the @Inject requirement. If {@code isDecorator} is false, any
     *
     * @Delegate found is immediately reported as an error (delegate outside decorator).
     *
     * @param owner The element to report diagnostics on (field or method).
     * @param element The actual element annotated with @Delegate.
     * @param isDecorator Whether the enclosing class is annotated with @Decorator.
     * @param methodAnnotations Annotations from the enclosing method, if any.
     */
    private void validateDelegate(IType type, IJavaElement owner, IJavaElement element, String uri,
                                  JavaDiagnosticsContext context, List<Diagnostic> diagnostics,
                                  List<IJavaElement> delegateElements, boolean isDecorator,
                                  IAnnotation... methodAnnotations) throws JavaModelException {

        IAnnotation[] annotations = (element instanceof IAnnotatable) ? ((IAnnotatable) element).getAnnotations() : new IAnnotation[0];

        if (!DiagnosticUtils.isMatchedAnnotation(type.getCompilationUnit(), annotations, Constants.DELEGATE_FQ_NAME)) {
            return;
        }

        if (isDecorator) {
            delegateElements.add(element);
            validateDelegateInjectionPoint(owner,
                                           methodAnnotations.length > 0 ? methodAnnotations : annotations,
                                           type, uri, context, diagnostics);
        } else {
            Range range = PositionUtils.toNameRange(element, context.getUtils());
            String message = Messages.getMessage("DelegateOutsideDecorator");
            diagnostics.add(context.createDiagnostic(uri, message, range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidDelegateOutsideDecorator,
                                                     DiagnosticSeverity.Error));
        }
    }

    /**
     * Reports diagnostics when a decorator has an invalid number of @Delegate injection points.
     *
     * @param type the decorator type being validated
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @param delegateElements the list of fields/parameters annotated with @Delegate
     * @param delegateCount the number of @Delegate injection points found
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void reportInvalidDelegateCountDiagnostics(IType type, String uri, JavaDiagnosticsContext context,
                                                       List<Diagnostic> diagnostics, List<IJavaElement> delegateElements,
                                                       int delegateCount) throws JavaModelException {
        // Report diagnostics based on delegate count
        if (delegateCount == 0) {
            // No @Delegate found - report at class level
            Range range = PositionUtils.toNameRange(type, context.getUtils());
            String message = Messages.getMessage("MissingDelegateInDecorator");
            diagnostics.add(context.createDiagnostic(uri, message, range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidDecoratorDelegateInjectionPoints,
                                                     DiagnosticSeverity.Error));
        } else if (delegateCount > 1) {
            // Multiple @Delegate found - report at each field/parameter level
            String message = Messages.getMessage("DecoratorWithMultipleDelegates", delegateCount);
            for (IJavaElement element : delegateElements) {
                Range range = PositionUtils.toNameRange(element, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri, message, range,
                                                         Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.InvalidDecoratorDelegateInjectionPoints,
                                                         DiagnosticSeverity.Error));
            }
        }
        // If delegateCount == 1, no diagnostic needed (valid case)
    }

    /**
     * Validates that an element annotated with @Delegate is also annotated with @Inject
     * (for fields) or is on a method/constructor annotated with @Inject (for parameters).
     *
     * @param diagnosticTarget the element where the diagnostic should be reported (field or method)
     * @param annotations the element's or containing method's annotations
     * @param type the declaring type
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateDelegateInjectionPoint(IJavaElement diagnosticTarget,
                                                IAnnotation[] annotations, IType type, String uri,
                                                JavaDiagnosticsContext context, List<Diagnostic> diagnostics) throws JavaModelException {
        // Check if element or its containing method/constructor has @Inject annotation
        if (!DiagnosticUtils.isMatchedAnnotation(type.getCompilationUnit(), annotations, Constants.INJECT_FQ_NAME)) {
            // @Delegate without @Inject - report diagnostic on the target element
            // For fields, target is the field itself; for parameters, target is the method
            Range range = PositionUtils.toNameRange(diagnosticTarget, context.getUtils());
            String message = Messages.getMessage("InvalidDelegateInjectionPoint");
            diagnostics.add(context.createDiagnostic(uri, message, range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidDelegateInjectionPoint,
                                                     DiagnosticSeverity.Error));
        }
    }

    /**
     * Validates that the delegate type implements or extends all decorated types of the decorator
     * with exactly the same type parameters.
     *
     * Per CDI 3.0 specification section 8.1.3:
     * "The delegate type of a decorator must implement or extend every decorated type
     * (with exactly the same type parameters). If the delegate type does not implement
     * or extend a decorated type of the decorator (or specifies different type parameters),
     * the container automatically detects the problem and treats it as a definition error."
     *
     * @param decoratorType the decorator class
     * @param delegateElement the delegate injection point (field or parameter)
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateDelegateTypeAssignability(IType decoratorType, IJavaElement delegateElement,
                                                   String uri, JavaDiagnosticsContext context,
                                                   List<Diagnostic> diagnostics) throws JavaModelException {
        try {
            // Get the raw type signature of the delegate (used to resolve the IType)
            String delegateRawTypeSig = null;
            String delegateFullTypeSig = null;
            if (delegateElement instanceof IField) {
                delegateFullTypeSig = ((IField) delegateElement).getTypeSignature();
            } else if (delegateElement instanceof ILocalVariable) {
                delegateFullTypeSig = ((ILocalVariable) delegateElement).getTypeSignature();
            }
            if (delegateFullTypeSig == null) {
                return;
            }
            // Strip type arguments to get the raw erased type name for IType resolution
            delegateRawTypeSig = Signature.getTypeErasure(delegateFullTypeSig);
            String delegateSimpleName = Signature.toString(delegateRawTypeSig);
            String delegateTypeName = ManagedBean.getFullyQualifiedClassName(decoratorType, delegateSimpleName);
            if (delegateTypeName == null) {
                return; // Cannot resolve delegate type, skip validation
            }
            IType delegateType = decoratorType.getJavaProject().findType(delegateTypeName);
            if (delegateType == null) {
                return; // Cannot resolve delegate type, skip validation
            }
            // Extract type arguments from the delegate field/parameter signature
            String[] delegateTypeArgs = Signature.getTypeArguments(delegateFullTypeSig);

            // Get all decorated types (interfaces and superclasses of the decorator) with their signatures
            List<DecoratedTypeInfo> decoratedTypes = getDecoratedTypes(decoratorType);
            if (decoratedTypes.isEmpty()) {
                return; // No decorated types to validate against
            }
            // Check if delegate type implements/extends all decorated types (and with matching type params)
            boolean hasError = false;
            for (DecoratedTypeInfo decorated : decoratedTypes) {
                if (!TypeHierarchyUtils.inheritsFrom(delegateType, decorated.fqName)) {
                    // Delegate type does not implement/extend this decorated type at all
                    hasError = true;
                    break;
                }
                // Delegate type is assignable; now check type parameters match exactly
                if (decorated.typeArgs.length > 0 && !typeArgsMatch(decorated.typeArgs, delegateTypeArgs)) {
                    hasError = true;
                    break;
                }
            }
            if (hasError) {
                Range range = PositionUtils.toNameRange(delegateElement, context.getUtils());
                String delegateTypeSimpleName = delegateType.getElementName();
                String message = Messages.getMessage("InvalidDecoratorDelegateTypeAssignability",
                                                     delegateTypeSimpleName);
                diagnostics.add(context.createDiagnostic(uri, message, range,
                                                         Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.InvalidDecoratorDelegateTypeAssignability,
                                                         DiagnosticSeverity.Error));
            }
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Error validating delegate type assignability", e);
        }
    }

    /**
     * Returns true when two type-argument signature arrays represent the same
     * types, comparing element by element after erasing to their binary names.
     *
     * <p>An empty {@code decoratedArgs} means the decorated type is raw/non-parameterized;
     * in that case any delegate type args are considered matching (no constraint).
     * If {@code delegateArgs} is empty but {@code decoratedArgs} is not, they do not match.
     *
     * @param decoratedArgs type arguments from the decorator's {@code implements} clause
     * @param delegateArgs type arguments from the delegate field/parameter signature
     * @return true if the arrays are element-wise equal after erasure
     */
    private boolean typeArgsMatch(String[] decoratedArgs, String[] delegateArgs) {
        if (decoratedArgs.length == 0) {
            return true; // Non-parameterized decorated type — no constraint on delegate type args
        }
        if (decoratedArgs.length != delegateArgs.length) {
            return false;
        }
        for (int i = 0; i < decoratedArgs.length; i++) {
            String erasedDecorated = Signature.getTypeErasure(decoratedArgs[i]);
            String erasedDelegate = Signature.getTypeErasure(delegateArgs[i]);
            if (!erasedDecorated.equals(erasedDelegate)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all decorated types of the decorator (interfaces and superclasses, excluding Object)
     * together with their type-argument signatures as declared on the {@code implements} clause.
     *
     * @param decoratorType the decorator class
     * @return list of {@link DecoratedTypeInfo} objects with FQN and type arguments
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private List<DecoratedTypeInfo> getDecoratedTypes(IType decoratorType) throws JavaModelException {
        List<DecoratedTypeInfo> decoratedTypes = new ArrayList<>();

        // Use getSuperInterfaceTypeSignatures() to preserve generic type arguments
        String[] interfaceTypeSigs = decoratorType.getSuperInterfaceTypeSignatures();
        for (String sig : interfaceTypeSigs) {
            // Extract type arguments from the signature (empty array when non-parameterized)
            String[] typeArgs = Signature.getTypeArguments(sig);
            // Resolve the raw (erased) interface name to its fully-qualified name
            String rawSig = Signature.getTypeErasure(sig);
            String simpleName = Signature.toString(rawSig);
            String fqName = ManagedBean.getFullyQualifiedClassName(decoratorType, simpleName);
            if (fqName != null) {
                decoratedTypes.add(new DecoratedTypeInfo(fqName, typeArgs));
            }
        }

        // Get superclass (excluding java.lang.Object)
        String superclassSig = decoratorType.getSuperclassTypeSignature();
        if (superclassSig != null) {
            String rawSig = Signature.getTypeErasure(superclassSig);
            String simpleName = Signature.toString(rawSig);
            if (!simpleName.equals("Object") && !simpleName.equals("java.lang.Object")) {
                String fqName = ManagedBean.getFullyQualifiedClassName(decoratorType, simpleName);
                if (fqName != null && !fqName.equals("java.lang.Object")) {
                    String[] typeArgs = Signature.getTypeArguments(superclassSig);
                    decoratedTypes.add(new DecoratedTypeInfo(fqName, typeArgs));
                }
            }
        }

        return decoratedTypes;
    }
}