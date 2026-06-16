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
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
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
public class DecoratorDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

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
            // Log and continue - don't let exception propagate
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
    private void validateDecorator(IType type, ICompilationUnit unit, String uri, JavaDiagnosticsContext context,
                                   List<Diagnostic> diagnostics) throws JavaModelException {
        // Check if the type is annotated with @Decorator
        String[] typeAnnotations = Stream.of(type.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new);
        List<String> decoratorAnnotations = DiagnosticUtils.getMatchedJavaElementNames(type, typeAnnotations,
                                                                                       new String[] { Constants.DECORATOR_FQ_NAME });

        if (decoratorAnnotations.isEmpty()) {
            return;
        }

        // Count @Delegate injection points across fields and method/constructor parameters
        int delegateCount = 0;

        // Check fields for @Delegate annotation
        IField[] fields = type.getFields();
        for (IField field : fields) {
            String[] fieldAnnotations = Stream.of(field.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new);
            List<String> delegateOnField = DiagnosticUtils.getMatchedJavaElementNames(type, fieldAnnotations,
                                                                                      new String[] { Constants.DELEGATE_FQ_NAME });
            if (!delegateOnField.isEmpty()) {
                delegateCount++;
            }
        }

        // Check constructor and method parameters for @Delegate annotation
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {
            ILocalVariable[] parameters = method.getParameters();
            for (ILocalVariable parameter : parameters) {
                String[] paramAnnotations = Stream.of(parameter.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new);
                List<String> delegateOnParam = DiagnosticUtils.getMatchedJavaElementNames(type, paramAnnotations,
                                                                                          new String[] { Constants.DELEGATE_FQ_NAME });
                if (!delegateOnParam.isEmpty()) {
                    delegateCount++;
                }
            }
        }

        // Report diagnostic if delegate count is not exactly 1
        if (delegateCount != 1) {
            Range range = PositionUtils.toNameRange(type, context.getUtils());
            String message;

            if (delegateCount == 0) {
                message = Messages.getMessage("DecoratorWithInvalidDelegateCount");
            } else {
                message = Messages.getMessage("DecoratorWithMultipleDelegates", delegateCount);
            }

            diagnostics.add(context.createDiagnostic(uri, message, range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidDecoratorDelegateInjectionPoints,
                                                     DiagnosticSeverity.Error));
        }
    }
}