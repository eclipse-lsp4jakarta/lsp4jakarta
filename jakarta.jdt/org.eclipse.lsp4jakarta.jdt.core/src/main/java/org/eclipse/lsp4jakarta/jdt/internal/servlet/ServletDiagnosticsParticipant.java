/*******************************************************************************
 * Copyright (c) 2023, 2025 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Pengyu Xiong - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.servlet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.TypeHierarchyUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Servlet diagnostic participant.
 *
 * @see https://jakarta.ee/specifications/servlet/5.0/jakarta-servlet-spec-5.0.html#webservlet
 */
public class ServletDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }
        IType[] alltypes;
        IAnnotation[] allAnnotations;

        alltypes = unit.getAllTypes();
        for (IType type : alltypes) {
            allAnnotations = type.getAnnotations();

            IAnnotation webServletAnnotation = null;
            for (IAnnotation annotation : allAnnotations) {
                if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                         Constants.WEB_SERVLET_FQ_NAME)) {
                    webServletAnnotation = annotation;
                    break; // get the first one, the annotation is not repeatable
                }
            }

            if (webServletAnnotation != null) {
                // check if the class extends HttpServlet
                try {
                    int r = TypeHierarchyUtils.doesITypeHaveSuperType(type, Constants.HTTP_SERVLET_FQ_NAME);
                    if (r == -1) {
                        Range range = PositionUtils.toNameRange(type, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("WebServletMustExtend"), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                                 ErrorCode.WebServletAnnotatedClassDoesNotExtendHttpServlet, DiagnosticSeverity.Error));
                    } else if (r == 0) { // unknown super type
                        Range range = PositionUtils.toNameRange(type, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("WebServletMustExtend"), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                                 ErrorCode.WebServletAnnotatedClassUnknownSuperTypeDoesNotExtendHttpServlet,
                                                                 DiagnosticSeverity.Warning));
                    }
                } catch (CoreException e) {
                    JakartaCorePlugin.logException("Cannot check type hierarchy", e);
                }

                /* URL pattern diagnostic check */
                IMemberValuePair[] memberValues = webServletAnnotation.getMemberValuePairs();

                boolean isUrlpatternSpecified = false;
                boolean isValueSpecified = false;
                for (IMemberValuePair mv : memberValues) {
                    if (mv.getMemberName().equals(Constants.URL_PATTERNS)) {
                        isUrlpatternSpecified = true;
                        continue;
                    }
                    if (mv.getMemberName().equals(Constants.VALUE)) {
                        isValueSpecified = true;
                    }
                }
                if (!isUrlpatternSpecified && !isValueSpecified) {
                    Range range = PositionUtils.toNameRange(webServletAnnotation, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("WebServletMustDefine"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.WebServletAnnotationMissingAttributes, DiagnosticSeverity.Error));
                }
                if (isUrlpatternSpecified && isValueSpecified) {
                    Range range = PositionUtils.toNameRange(webServletAnnotation, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("WebServletCannotHaveBoth"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.WebServletAnnotationAttributeConflict, DiagnosticSeverity.Error));
                }
            }
        }

        return diagnostics;
    }

}