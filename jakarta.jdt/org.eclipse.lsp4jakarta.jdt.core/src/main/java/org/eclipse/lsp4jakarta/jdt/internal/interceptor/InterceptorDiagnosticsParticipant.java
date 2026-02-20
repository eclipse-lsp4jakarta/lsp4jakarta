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
*     IBM Corporation, Archana Iyer R - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
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
 * Interceptor diagnostic participant that manages the use of @Interceptor annotation.
 */
public class InterceptorDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(InterceptorDiagnosticsParticipant.class.getName());

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            Map<String, Boolean> constructorInfo = new HashMap<>();
            constructorInfo.put("hasConstructor", false);
            constructorInfo.put("hasValidPublicNoArgsConstructor", false);
            int typeFlag = type.getFlags();
            boolean isInterceptorType = Arrays.stream(type.getAnnotations()).filter(Objects::nonNull).anyMatch(annotation -> {
                try {
                    return DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), Constants.INTERCEPTOR_FQ_NAME);
                } catch (JavaModelException e) {
                    LOGGER.log(Level.WARNING, "Unable to find matching annotation", e.getMessage());
                    return false;
                }
            });
            if (isInterceptorType) {
                Range range = PositionUtils.toNameRange(type, context.getUtils());
                if (Flags.isAbstract(typeFlag)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("InvalidInterceptorAbstractClass", type.getElementName()), range,
                                                             Constants.DIAGNOSTIC_SOURCE, ErrorCode.InvalidInterceptorAnnotationOnAbstractClass, DiagnosticSeverity.Error));
                }
                for (IMethod method : type.getMethods()) {
                    //Checks if method is a constructor and has valid no-args constructor
                    constructorInfo = DiagnosticUtils.hasValidNoArgsConstructor(method, constructorInfo);
                }
                // Conditions for checking missing public no-args constructor
                if (!constructorInfo.get("hasValidPublicNoArgsConstructor") && constructorInfo.get("hasConstructor")) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ErrorMessageInterceptorNoArgConstructorMissing", type.getElementName()), range,
                                                             Constants.DIAGNOSTIC_SOURCE, ErrorCode.InvalidInterceptorNoArgsConstructorMissing, DiagnosticSeverity.Error));
                }
            }
        }
        return diagnostics;
    }
}