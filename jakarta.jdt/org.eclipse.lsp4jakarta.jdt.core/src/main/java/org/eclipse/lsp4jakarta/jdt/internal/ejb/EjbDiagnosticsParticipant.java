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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
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

import com.google.gson.Gson;

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

                // Check for @Interceptor or @Decorator annotations
                List<String> invalidAnnotations = DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                                             type.getAnnotations(),
                                                                                             new String[] {
                                                                                                            Constants.INTERCEPTOR_FQ_NAME,
                                                                                                            Constants.DECORATOR_FQ_NAME
                                                                                             });

                if (!invalidAnnotations.isEmpty()) {
                    String message = Messages.getMessage("InvalidSessionBeanWithInterceptorOrDecorator");
                    diagnostics.add(context.createDiagnostic(uri, message, range,
                                                             Constants.DIAGNOSTIC_SOURCE,
                                                             ErrorCode.InvalidSessionBeanWithInterceptorOrDecorator,
                                                             DiagnosticSeverity.Error));
                }

                if (sessionBeanAnnotations.size() > 1) {
                    String annotationNames = sessionBeanAnnotations.stream().map(DiagnosticUtils::getSimpleName).map(name -> "@" + name).collect(Collectors.joining(", "));
                    String message = Messages.getMessage("SessionBeanConflictingAnnotations", annotationNames);
                    diagnostics.add(context.createDiagnostic(uri, message, range,
                                                             Constants.DIAGNOSTIC_SOURCE,
                                                             (new Gson().toJsonTree(sessionBeanAnnotations)),
                                                             ErrorCode.ConflictingSessionBeanAnnotations,
                                                             DiagnosticSeverity.Error));
                }

                validateSessionBeanConstructor(type, context, uri, diagnostics);
                validateSessionBeanFinalizeMethod(type, context, uri, diagnostics);
                // Validate session synchronization methods (@AfterBegin, @BeforeCompletion, @AfterCompletion)
                validateSessionSyncMethods(context, uri, unit, type, diagnostics);
            }
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

            String annotationNames = DiagnosticUtils.getSimpleAnnotationNames(matchedAnnotations, "@");
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

            if (!Constants.VOID_RETURN_TYPE.equals(method.getReturnType())) {
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
        String[] methodAnnotationNames = Stream.of(method.getAnnotations()).map(IAnnotation::getElementName).toArray(String[]::new);
        return DiagnosticUtils.getMatchedJavaElementNames(type, methodAnnotationNames,
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
        return Constants.BOOLEAN_PRIMITIVE_SIGNATURE.equals(param)
               || DiagnosticUtils.isMatchedJavaElement(type, DiagnosticUtils.getDataTypeName(param), Constants.BOOLEAN_FQ_NAME);
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
