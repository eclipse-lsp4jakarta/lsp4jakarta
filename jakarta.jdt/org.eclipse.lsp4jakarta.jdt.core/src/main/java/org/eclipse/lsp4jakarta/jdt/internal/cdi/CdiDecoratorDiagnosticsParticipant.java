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
     * Validates that the delegate type implements or extends all decorated types of the decorator.
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
            String delegateTypeName = null;
            if (delegateElement instanceof IField) {
                String typeSignature = Signature.toString(((IField) delegateElement).getTypeSignature());
                delegateTypeName = ManagedBean.getFullyQualifiedClassName(decoratorType, typeSignature);
            } else if (delegateElement instanceof ILocalVariable) {
                String simpleTypeName = Signature.toString(((ILocalVariable) delegateElement).getTypeSignature());
                delegateTypeName = ManagedBean.getFullyQualifiedClassName(decoratorType, simpleTypeName);
            }
            if (delegateTypeName == null) {
                return; // Cannot resolve delegate type, skip validation
            }
            IType delegateType = decoratorType.getJavaProject().findType(delegateTypeName);
            if (delegateType == null) {
                return; // Cannot resolve delegate type, skip validation
            }
            // Get all decorated types (interfaces and superclasses of the decorator)
            List<String> decoratedTypes = getDecoratedTypes(decoratorType);
            if (decoratedTypes.isEmpty()) {
                return; // No decorated types to validate against
            }
            // Check if delegate type implements/extends all decorated types
            List<String> missingTypes = new ArrayList<>();
            for (String decoratedTypeFQN : decoratedTypes) {
                // Use TypeHierarchyUtils.inheritsFrom for checking (more efficient and robust)
                if (!TypeHierarchyUtils.inheritsFrom(delegateType, decoratedTypeFQN)) {
                    missingTypes.add(decoratedTypeFQN);
                }
            }
            // Report diagnostic if delegate type doesn't implement all decorated types
            if (!missingTypes.isEmpty()) {
                Range range = PositionUtils.toNameRange(delegateElement, context.getUtils());
                // Use simple class names for better readability
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
     * Gets all decorated types of the decorator (interfaces and superclasses, excluding Object).
     *
     * @param decoratorType the decorator class
     * @return list of decorated type fully qualified names
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private List<String> getDecoratedTypes(IType decoratorType) throws JavaModelException {
        List<String> decoratedTypes = new ArrayList<>();

        // Get all interfaces implemented by the decorator
        String[] interfaceNames = decoratorType.getSuperInterfaceNames();
        for (String interfaceName : interfaceNames) {
            String fqName = ManagedBean.getFullyQualifiedClassName(decoratorType, interfaceName);
            if (fqName != null) {
                decoratedTypes.add(fqName);
            }
        }
        // Get superclass (excluding java.lang.Object)
        String superclassName = decoratorType.getSuperclassName();
        if (superclassName != null && !superclassName.equals("Object")) {
            String fqName = ManagedBean.getFullyQualifiedClassName(decoratorType, superclassName);
            if (fqName != null && !fqName.equals("java.lang.Object")) {
                decoratedTypes.add(fqName);
            }
        }

        return decoratedTypes;
    }
}