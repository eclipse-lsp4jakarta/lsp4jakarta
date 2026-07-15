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
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * CDI diagnostics participant that validates decorator delegate injection points.
 *
 * A decorator must declare exactly one injection point annotated with @Delegate.
 *
 * @see https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#delegate_attribute
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
                validateDecorator(type, unit, uri, context, diagnostics);
            }
        } catch (JavaModelException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while validating decorator", e);
        }

        return diagnostics;
    }

    /**
     * Validates that a decorator class declares exactly one @Delegate injection point.
     *
     * @param type the type to validate
     * @param unit the compilation unit
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateDecorator(IType type, ICompilationUnit unit, String uri,
                                   JavaDiagnosticsContext context, List<Diagnostic> diagnostics) throws JavaModelException {

        if (!DiagnosticUtils.isMatchedAnnotation(unit, type.getAnnotations(), Constants.DECORATOR_FQ_NAME)) {
            return;
        }

        List<IJavaElement> delegateElements = new ArrayList<>();
        for (IField field : type.getFields()) {
            validateDelegate(type, field, field, uri, context, diagnostics, delegateElements);
        }
        for (IMethod method : type.getMethods()) {
            IAnnotation[] methodAnnotations = method.getAnnotations();

            for (ILocalVariable parameter : method.getParameters()) {
                validateDelegate(type, method, parameter, uri, context, diagnostics, delegateElements, methodAnnotations);
            }
        }
        reportInvalidDelegateCountDiagnostics(type, uri, context, diagnostics,
                                              delegateElements, delegateElements.size());
    }

    /**
     * Unified delegate processing for fields and parameters.
     *
     * @param owner The element to report diagnostics on (field or method).
     * @param element The actual element annotated with @Delegate.
     */
    private void validateDelegate(IType type, IJavaElement owner, IJavaElement element, String uri,
                                  JavaDiagnosticsContext context, List<Diagnostic> diagnostics,
                                  List<IJavaElement> delegateElements, IAnnotation... methodAnnotations) throws JavaModelException {

        IAnnotation[] annotations = (element instanceof IAnnotatable) ? ((IAnnotatable) element).getAnnotations() : new IAnnotation[0];

        if (DiagnosticUtils.isMatchedAnnotation(type.getCompilationUnit(), annotations, Constants.DELEGATE_FQ_NAME)) {
            delegateElements.add(element);
            validateDelegateInjectionPoint(owner,
                                           methodAnnotations.length > 0 ? methodAnnotations : annotations,
                                           type, uri, context, diagnostics);
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
}