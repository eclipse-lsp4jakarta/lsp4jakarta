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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.TypeHierarchyUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * CDI diagnostics participant that validates decorator delegate injection points.
 *
 * Validates:
 * 1. A decorator must declare exactly one injection point annotated with @Delegate
 * 2. The delegate type must implement or extend all decorated types of the decorator,
 * with exactly the same type parameters
 *
 * @see https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#delegate_attribute
 * @see https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#decorator_bean
 */
public class CdiDecoratorDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

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
            validateDelegate(type, field, field, uri, context, diagnostics, delegateElements, false, isDecorator);
        }
        for (IMethod method : type.getMethods()) {
            IAnnotation[] methodAnnotations = method.getAnnotations();

            // Per CDI spec §3.7 / §8.1.2, @Delegate is only valid on injection points:
            // fields, bean constructor parameters, or initializer method parameters.
            // An initializer method is a non-constructor, non-static, void method
            // annotated with @Inject.
            boolean isConstructor = DiagnosticUtils.isConstructorMethod(method);
            boolean isInitializerMethod = !isConstructor
                                          && !Flags.isStatic(method.getFlags())
                                          && JDTTypeUtils.isVoidReturnType(method)
                                          && DiagnosticUtils.isMatchedAnnotation(type.getCompilationUnit(), methodAnnotations, Constants.INJECT_FQ_NAME);
            // True when the method is not a valid @Delegate injection-point context.
            // Parameters of such methods still need to be scanned so that any @Delegate
            // annotation on them produces InvalidDelegateOnNonInjectionPoint.
            boolean isNonInjectionPointMethod = !isConstructor && !isInitializerMethod;

            for (ILocalVariable parameter : method.getParameters()) {
                validateDelegate(type, method, parameter, uri, context, diagnostics, delegateElements,
                                 isNonInjectionPointMethod, isDecorator, methodAnnotations);
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
     * @param isNonInjectionPointMethod true when the enclosing method is not a valid
     *            injection-point context (not a constructor and not an @Inject void method).
     *            When true the element is NOT counted as a delegate injection point but
     *            InvalidDelegateOnNonInjectionPoint is reported if @Delegate is present.
     * @param isDecorator Whether the enclosing class is annotated with @Decorator.
     * @param methodAnnotations Annotations from the enclosing method, if any.
     */
    private void validateDelegate(IType type, IJavaElement owner, IJavaElement element, String uri,
                                  JavaDiagnosticsContext context, List<Diagnostic> diagnostics,
                                  List<IJavaElement> delegateElements, boolean isNonInjectionPointMethod,
                                  boolean isDecorator,
                                  IAnnotation... methodAnnotations) throws JavaModelException {

        IAnnotation[] annotations = (element instanceof IAnnotatable) ? ((IAnnotatable) element).getAnnotations() : new IAnnotation[0];

        if (!DiagnosticUtils.isMatchedAnnotation(type.getCompilationUnit(), annotations, Constants.DELEGATE_FQ_NAME)) {
            return;
        }

        if (isDecorator) {
            delegateElements.add(element);
            validateDelegateInjectionPoint(owner,
                                           methodAnnotations.length > 0 ? methodAnnotations : annotations,
                                           type, uri, context, diagnostics, isNonInjectionPointMethod);
        } else {
            Range range = PositionUtils.toNameRange(element, context.getUtils());
            String message = Messages.getMessage("InvalidDelegateOutsideDecorator");
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
     * @param delegateCount the number of valid @Delegate injection points found
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void reportInvalidDelegateCountDiagnostics(IType type, String uri, JavaDiagnosticsContext context,
                                                       List<Diagnostic> diagnostics, List<IJavaElement> delegateElements,
                                                       int delegateCount) throws JavaModelException {
        // Report diagnostics based on delegate count
        if (delegateCount == 0) {
            // No valid @Delegate injection point found - report at class level
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
     * @param isNonInjectionPointMethod true when the enclosing method is not a valid injection-point
     *            context; in that case the missing-@Inject diagnostic is reported regardless of whether
     * @Inject is present on the method (the method itself is not a valid injection point)
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateDelegateInjectionPoint(IJavaElement diagnosticTarget,
                                                IAnnotation[] annotations, IType type, String uri,
                                                JavaDiagnosticsContext context, List<Diagnostic> diagnostics,
                                                boolean isNonInjectionPointMethod) throws JavaModelException {
        if (!DiagnosticUtils.isMatchedAnnotation(type.getCompilationUnit(), annotations, Constants.INJECT_FQ_NAME)) {
            // @Inject is missing — offer "Insert @Inject" quickfix regardless of method kind.
            Range range = PositionUtils.toNameRange(diagnosticTarget, context.getUtils());
            String message = Messages.getMessage("InvalidDelegateInjectionPoint");
            diagnostics.add(context.createDiagnostic(uri, message, range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidDelegateInjectionPoint,
                                                     DiagnosticSeverity.Error));
        } else if (isNonInjectionPointMethod) {
            // @Inject is present but the method is not a valid injection-point context
            // (e.g. non-void, static). No quickfix — the user needs to fix the method itself.
            Range range = PositionUtils.toNameRange(diagnosticTarget, context.getUtils());
            String message = Messages.getMessage("InvalidDelegateOnNonInjectionPoint");
            diagnostics.add(context.createDiagnostic(uri, message, range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidDelegateOnNonInjectionPoint,
                                                     DiagnosticSeverity.Error));
        }
    }

    /**
     * Validates that the delegate type implements or extends all decorated types of the decorator.
     *
     * Per CDI 3.0 specification section 8.1.3:
     * "The delegate type of a decorator must implement or extend every decorated type
     * (with exactly the same type parameters). If the delegate type does not implement
     * or extend a decorated type of the decorator (or specifies different type parameters),
     * the container automatically detects the problem and treats it as a definition error."
     *
     * @param decoratorClass the decorator class
     * @param delegateElement the delegate injection point (field or parameter)
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateDelegateTypeAssignability(IType decoratorClass, IJavaElement delegateElement,
                                                   String uri, JavaDiagnosticsContext context,
                                                   List<Diagnostic> diagnostics) throws JavaModelException {
        try {
            String rawTypeSignature = null;
            if (delegateElement instanceof IField) {
                rawTypeSignature = ((IField) delegateElement).getTypeSignature();
            } else if (delegateElement instanceof ILocalVariable) {
                rawTypeSignature = ((ILocalVariable) delegateElement).getTypeSignature();
            }
            if (rawTypeSignature == null) {
                return;
            }
            // Primitives are never valid bean types — report immediately without further resolution.
            if (Signature.getTypeSignatureKind(rawTypeSignature) == Signature.BASE_TYPE_SIGNATURE) {
                reportDecoratorDiagnostic(delegateElement, ErrorCode.InvalidDecoratorDelegateTypeAssignability.name(),
                                          Signature.toString(rawTypeSignature), "",
                                          ErrorCode.InvalidDecoratorDelegateTypeAssignability,
                                          uri, context, diagnostics);
                return;
            }
            IType delegateType = DiagnosticUtils.resolveTypeFromSignature(decoratorClass, rawTypeSignature);
            if (delegateType == null) {
                return; // Cannot resolve delegate type, skip validation
            }
            // Get all decorated types (interfaces of the decorator, excluding java.io.Serializable)
            List<String> decoratedTypes = getDecoratedTypes(decoratorClass);
            if (decoratedTypes.isEmpty()) {
                // Decorator has no decorated types — definition error
                reportDecoratorDiagnostic(delegateElement, ErrorCode.InvalidDecoratorWithNoDecoratedTypes.name(),
                                          null, null, ErrorCode.InvalidDecoratorWithNoDecoratedTypes,
                                          uri, context, diagnostics);
                return;
            }
            // Check if delegate type implements/extends all decorated types
            for (String decoratedTypeFQN : decoratedTypes) {
                if (!TypeHierarchyUtils.inheritsFrom(delegateType, decoratedTypeFQN)) {
                    reportDecoratorDiagnostic(delegateElement, ErrorCode.InvalidDecoratorDelegateTypeAssignability.name(),
                                              delegateType.getElementName(), DiagnosticUtils.getSimpleName(decoratedTypeFQN),
                                              ErrorCode.InvalidDecoratorDelegateTypeAssignability,
                                              uri, context, diagnostics);
                    return;
                }
            }
            if (hasTypeParamMismatch(decoratorClass, delegateType, rawTypeSignature)) {
                reportDecoratorDiagnostic(delegateElement, ErrorCode.InvalidDelegateDecoratedTypeParamMismatch.name(),
                                          delegateType.getElementName(), delegateType.getElementName(),
                                          ErrorCode.InvalidDelegateDecoratedTypeParamMismatch,
                                          uri, context, diagnostics);
            }
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Error validating delegate type assignability", e);
        }
    }

    /**
     * Returns true when the delegate is declared as one of the decorator's directly implemented
     * parameterized interfaces but with different type arguments (e.g. {@code Processor<Object>}
     * delegate for a decorator implementing {@code Processor<String>}).
     *
     * The comparison is only possible when the delegate is declared as the decorated type itself;
     * for a subtype the type parameters are fixed by its own declaration.
     *
     * @param decoratorClass the decorator class
     * @param delegateType the resolved delegate type
     * @param delegateTypeSignature the delegate's declared type signature, including type arguments
     * @return true if the type arguments differ
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private boolean hasTypeParamMismatch(IType decoratorClass, IType delegateType,
                                         String delegateTypeSignature) throws JavaModelException {
        String[] delegateTypeArgs = Signature.getTypeArguments(delegateTypeSignature);
        if (delegateTypeArgs.length == 0) {
            return false;
        }
        String delegateFQN = delegateType.getFullyQualifiedName('.');
        for (String decoratedInterfaceSignature : decoratorClass.getSuperInterfaceTypeSignatures()) {
            String[] decoratedArgs = Signature.getTypeArguments(decoratedInterfaceSignature);
            if (decoratedArgs.length > 0
                && delegateFQN.equals(JDTTypeUtils.getResolvedTypeName(decoratedInterfaceSignature, decoratorClass))
                && !typeArgsMatch(decoratorClass, decoratedArgs, delegateTypeArgs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reports a decorator validation diagnostic on the given delegate element.
     *
     * @param delegateElement the delegate injection point (field or parameter)
     * @param messageKey the message key (e.g., "InvalidDecoratorDelegateTypeAssignability")
     * @param delegateTypeName the simple name of the delegate type (may be null)
     * @param decoratedTypeName the simple name of the decorated type (may be empty/null)
     * @param errorCode the error code for the diagnostic
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add the diagnostic to
     */
    private void reportDecoratorDiagnostic(IJavaElement delegateElement, String messageKey,
                                           String delegateTypeName, String decoratedTypeName,
                                           ErrorCode errorCode, String uri,
                                           JavaDiagnosticsContext context,
                                           List<Diagnostic> diagnostics) throws JavaModelException {
        Range range = PositionUtils.toNameRange(delegateElement, context.getUtils());
        String message = Messages.getMessage(messageKey, delegateTypeName, decoratedTypeName);
        diagnostics.add(context.createDiagnostic(uri, message, range,
                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                 errorCode,
                                                 DiagnosticSeverity.Error));
    }

    /**
     * Returns true when both type-argument signature arrays refer to the same types, element by element.
     * Type names are resolved against the decorator class so that simple and qualified names compare equal.
     * Wildcards and names that cannot be resolved (e.g. type variables) are compared by signature text.
     *
     * @param decoratorClass the decorator class used to resolve type names
     * @param decoratedArgs type arguments from the decorator's {@code implements} clause
     * @param delegateArgs type arguments from the delegate injection point
     * @return true if the type arguments are the same
     */
    private boolean typeArgsMatch(IType decoratorClass, String[] decoratedArgs, String[] delegateArgs) {
        return resolveTypeArguments(decoratorClass, decoratedArgs).equals(resolveTypeArguments(decoratorClass, delegateArgs));
    }

    private List<String> resolveTypeArguments(IType decoratorClass, String[] typeArgSignatures) {
        return Arrays.stream(typeArgSignatures).map(sig -> {
            if (Signature.getTypeSignatureKind(sig) == Signature.WILDCARD_TYPE_SIGNATURE) {
                return sig;
            }
            String resolved = JDTTypeUtils.getResolvedTypeName(sig, decoratorClass);
            return resolved != null ? resolved : sig;
        }).collect(Collectors.toList());
    }

    /**
     * Gets all decorated types of the decorator (Java interfaces only, excluding java.io.Serializable).
     *
     * Per CDI 3.0 specification section 8.1.3:
     * "The set of decorated types of a decorator includes all bean types of the managed bean
     * which are Java interfaces, except for java.io.Serializable. The decorator bean class and
     * its superclasses are not decorated types of the decorator."
     *
     * @param decoratorClass the decorator class
     * @return list of decorated type fully qualified names (interfaces only)
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private List<String> getDecoratedTypes(IType decoratorClass) throws JavaModelException {
        List<String> decoratedTypes = new ArrayList<>();

        // Get all interfaces implemented by the decorator and its superclasses (transitively)
        IType[] interfaces = TypeHierarchyUtils.getAllInterfaces(decoratorClass);
        for (IType interfaceType : interfaces) {
            String fqName = interfaceType.getFullyQualifiedName();
            if (!Constants.SERIALIZABLE_FQ_NAME.equals(fqName)) {
                decoratedTypes.add(fqName);
            }
        }
        return decoratedTypes;
    }
}
