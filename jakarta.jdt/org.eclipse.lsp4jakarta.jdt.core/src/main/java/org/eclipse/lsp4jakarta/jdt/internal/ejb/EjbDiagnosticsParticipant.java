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
*     IBM Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.ejb;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.helpers.ConstructorInfoDiagnosticHelper;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * EJB diagnostic participant that validates session beans.
 */
public class EjbDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            // Check if the operation has been cancelled by the user
            if (monitor.isCanceled()) {
                return null;
            }

            if (!type.isClass()) {
                continue;
            }

            String[] typeAnnotations = Stream.of(type.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new);
            List<String> sessionBeanAnnotations = DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                                             typeAnnotations,
                                                                                             Constants.SESSION_BEAN_ANNOTATIONS);

            if (!sessionBeanAnnotations.isEmpty()) {
                int typeFlags = type.getFlags();
                Range range = PositionUtils.toNameRange(type, context.getUtils());

                // Check: class must be public
                if (!Flags.isPublic(typeFlags)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("SessionBeanMustBePublic"),
                                                             range, Constants.DIAGNOSTIC_SOURCE,
                                                             ErrorCode.InvalidModifierNotPublic,
                                                             DiagnosticSeverity.Error));
                }

                // Check: class must not be final
                if (Flags.isFinal(typeFlags)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("SessionBeanMustNotBeFinal"),
                                                             range, Constants.DIAGNOSTIC_SOURCE,
                                                             ErrorCode.InvalidModifierFinal,
                                                             DiagnosticSeverity.Error));
                }

                // Check: class must not be abstract
                if (Flags.isAbstract(typeFlags)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("SessionBeanMustNotBeAbstract"),
                                                             range, Constants.DIAGNOSTIC_SOURCE,
                                                             ErrorCode.InvalidModifierAbstract,
                                                             DiagnosticSeverity.Error));
                }

                // Check: class must be a top-level class (not nested/inner/anonymous/local)
                if (type.isMember() || type.isAnonymous() || type.isLocal()) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("SessionBeanMustBeTopLevel"),
                                                             range, Constants.DIAGNOSTIC_SOURCE,
                                                             ErrorCode.InvalidNonTopLevelClass,
                                                             DiagnosticSeverity.Error));
                }
                validateSessionBeanConstructor(type, context, uri, diagnostics);
                validateSessionBeanFinalizeMethod(type, context, uri, diagnostics);
            }
        }

        return diagnostics;
    }

    /**
     * Validates that the session bean has a valid public no-arg constructor.
     *
     * @param type the type to validate
     * @param context the diagnostics context
     * @param uri the URI of the file
     * @param diagnostics the list to add diagnostics to
     * @throws CoreException if an error occurs
     */
    private void validateSessionBeanConstructor(IType type, JavaDiagnosticsContext context, String uri,
                                                List<Diagnostic> diagnostics) throws CoreException {
        ConstructorInfoDiagnosticHelper constructorInfo = ConstructorInfoDiagnosticHelper.getConstructorInfo(type);

        if (constructorInfo.hasConstructor() && !constructorInfo.hasValidPublicNoArgsConstructor()) {
            String message = Messages.getMessage("SessionBeanNoArgConstructor");
            Range range = PositionUtils.toNameRange(type, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri, message, range,
                                                     Constants.DIAGNOSTIC_SOURCE,
                                                     ErrorCode.MissingPublicNoArgConstructor,
                                                     DiagnosticSeverity.Error));
        }
    }

    /**
     * Validates that the session bean does not define or override the finalize() method.
     *
     * @param type the type to validate
     * @param context the diagnostics context
     * @param uri the URI of the file
     * @param diagnostics the list to add diagnostics to
     * @throws CoreException if an error occurs
     */
    private void validateSessionBeanFinalizeMethod(IType type, JavaDiagnosticsContext context, String uri,
                                                   List<Diagnostic> diagnostics) throws CoreException {
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {
            if (Constants.FINALIZE_METHOD_NAME.equals(method.getElementName())
                && method.getNumberOfParameters() == 0) {
                String message = Messages.getMessage("SessionBeanFinalizeMethod");
                Range range = PositionUtils.toNameRange(method, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri, message, range,
                                                         Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.SessionBeanFinalizeMethod,
                                                         DiagnosticSeverity.Error));
            }
        }
    }
}
