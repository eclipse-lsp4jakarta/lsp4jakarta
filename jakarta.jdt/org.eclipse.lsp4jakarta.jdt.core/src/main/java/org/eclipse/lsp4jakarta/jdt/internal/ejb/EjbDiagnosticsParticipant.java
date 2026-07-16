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

            List<String> sessionBeanAnnotations = DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                                             type.getAnnotations(),
                                                                                             Constants.SESSION_BEAN_ANNOTATIONS);

            if (!sessionBeanAnnotations.isEmpty()) {
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

            // Validate session synchronization methods (@AfterBegin, @BeforeCompletion, @AfterCompletion)
            validateSessionSyncMethods(context, uri, unit, type, diagnostics);
        }

        return diagnostics;
    }

    /**
     * Validates that session synchronization methods on a type comply with the EJB spec:
     * must not be final, must not be static, must return void, and must declare the correct parameters.
     *
     * @param context the diagnostics context
     * @param uri the file URI
     * @param unit the compilation unit
     * @param type the type to inspect
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if there is an error accessing the Java model
     */
    private void validateSessionSyncMethods(JavaDiagnosticsContext context, String uri,
                                            ICompilationUnit unit, IType type,
                                            List<Diagnostic> diagnostics) throws JavaModelException {
        for (IMethod method : type.getMethods()) {
            List<String> matchedAnnotations = getSessionSyncAnnotations(unit, type, method);
            if (matchedAnnotations.isEmpty()) {
                continue;
            }

            String annotationNames = getSimpleAnnotationNames(matchedAnnotations);
            int flags = method.getFlags();

            if (Flags.isFinal(flags)) {
                Range range = PositionUtils.toNameRange(method, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("InvalidSessionSyncMethodFinal", annotationNames),
                                                         range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InvalidSessionSyncMethodFinal,
                                                         DiagnosticSeverity.Error));
            }

            if (Flags.isStatic(flags)) {
                Range range = PositionUtils.toNameRange(method, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("InvalidSessionSyncMethodStatic", annotationNames),
                                                         range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InvalidSessionSyncMethodStatic,
                                                         DiagnosticSeverity.Error));
            }

            if (!"V".equals(method.getReturnType())) {
                Range range = PositionUtils.toNameRange(method, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("InvalidSessionSyncMethodNonVoid", annotationNames),
                                                         range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InvalidSessionSyncMethodNonVoid,
                                                         DiagnosticSeverity.Error));
            }

            // Validate @AfterBegin/@BeforeCompletion: no parameters allowed
            boolean isNoParamAnnotation = !DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                                      method.getAnnotations(),
                                                                                      Constants.SESSION_SYNC_NO_PARAM_ANNOTATIONS).isEmpty();

            if (isNoParamAnnotation && method.getNumberOfParameters() > 0) {
                Range range = PositionUtils.toNameRange(method, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("InvalidSessionSyncMethodNoParamAnnotation", annotationNames),
                                                         range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InvalidSessionSyncMethodNoParamAnnotation,
                                                         DiagnosticSeverity.Error));
            }

            // Validate @AfterCompletion: must have exactly one boolean parameter
            if (DiagnosticUtils.isMatchedAnnotation(unit, method.getAnnotations(),
                                                    Constants.AFTER_COMPLETION_FQ_NAME)
                && !isValidAfterCompletionParams(unit, type, method)) {
                Range range = PositionUtils.toNameRange(method, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("InvalidAfterCompletionMethodParams"),
                                                         range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InvalidAfterCompletionMethodParams,
                                                         DiagnosticSeverity.Error));
            }
        }
    }

    /**
     * Returns the list of session synchronization annotation FQ names present on
     * the given method.
     *
     * @param unit the compilation unit
     * @param type the declaring type
     * @param method the method to check
     * @return matched session sync annotation FQ names, never null
     * @throws JavaModelException if there is an error accessing the Java model
     */
    private List<String> getSessionSyncAnnotations(ICompilationUnit unit, IType type,
                                                   IMethod method) throws JavaModelException {
        return DiagnosticUtils.getMatchedJavaElementNames(type, method.getAnnotations(),
                                                          Constants.SESSION_SYNC_ANNOTATIONS);
    }

    /**
     * Returns true if the given {@code @AfterCompletion} method has exactly one
     * {@code boolean} (or {@code Boolean}) parameter, as required by the EJB spec.
     *
     * @param unit the compilation unit
     * @param type the declaring type
     * @param method the method to check
     * @return true if the parameter signature is valid
     * @throws JavaModelException if there is an error accessing the Java model
     */
    private boolean isValidAfterCompletionParams(ICompilationUnit unit, IType type,
                                                 IMethod method) throws JavaModelException {
        String[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1) {
            return false;
        }
        // JDT type signature: "Z" = boolean, "QBoolean;" = java.lang.Boolean
        String param = paramTypes[0];
        return "Z".equals(param)
               || DiagnosticUtils.isMatchedJavaElement(type, DiagnosticUtils.getDataTypeName(param), "java.lang.Boolean");
    }

    /**
     * Converts a list of fully qualified annotation names to a comma-separated
     * string of simple names prefixed with {@code @}.
     *
     * @param annotations the FQ annotation names
     * @return display string, e.g. "@AfterBegin"
     */
    private String getSimpleAnnotationNames(List<String> annotations) {
        return annotations.stream().map(fq -> "@" + DiagnosticUtils.getSimpleName(fq)).distinct().collect(java.util.stream.Collectors.joining(", "));
    }
}
