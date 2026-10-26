/*******************************************************************************
* Copyright (c) 2022, 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Giancarlo Pernudi Segura - initial API and implementation
*     Lidia Ataupillco Ramos
*     Aviral Saxena
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.websocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.helpers.ConstructorInfoDiagnosticHelper;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

import com.google.gson.JsonArray;

/**
 * WebSocket Diagnostic participant.
 */
public class WebSocketDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /**
     * Lookup table mapping both FQN and simple WebSocket message-type names
     * to their {@link Constants.MESSAGE_FORMAT}.
     */
    private static final Map<String, Constants.MESSAGE_FORMAT> MESSAGE_FORMAT_MAP = new HashMap<>();
    static {
        MESSAGE_FORMAT_MAP.put(Constants.STRING_CLASS_LONG, Constants.MESSAGE_FORMAT.TEXT);
        MESSAGE_FORMAT_MAP.put(Constants.READER_CLASS_LONG, Constants.MESSAGE_FORMAT.TEXT);
        MESSAGE_FORMAT_MAP.put(Constants.BYTEBUFFER_CLASS_LONG, Constants.MESSAGE_FORMAT.BINARY);
        MESSAGE_FORMAT_MAP.put(Constants.INPUTSTREAM_CLASS_LONG, Constants.MESSAGE_FORMAT.BINARY);
        MESSAGE_FORMAT_MAP.put(Constants.PONGMESSAGE_CLASS_LONG, Constants.MESSAGE_FORMAT.PONG);
        MESSAGE_FORMAT_MAP.put(Constants.STRING_CLASS_SHORT, Constants.MESSAGE_FORMAT.TEXT);
        MESSAGE_FORMAT_MAP.put(Constants.READER_CLASS_SHORT, Constants.MESSAGE_FORMAT.TEXT);
        MESSAGE_FORMAT_MAP.put(Constants.BYTEBUFFER_CLASS_SHORT, Constants.MESSAGE_FORMAT.BINARY);
        MESSAGE_FORMAT_MAP.put(Constants.INPUTSTREAM_CLASS_SHORT, Constants.MESSAGE_FORMAT.BINARY);
        MESSAGE_FORMAT_MAP.put(Constants.PONGMESSAGE_CLASS_SHORT, Constants.MESSAGE_FORMAT.PONG);
    }

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

        for (IType type : unit.getAllTypes()) {
            if (!isWSAnnotatedEndpoint(type)) {
                continue;
            }

            List<String> endpointPathVars = checkTypeAnnotations(context, uri, type, diagnostics);

            publicNoArgsConstructorCheck(context, uri, type, diagnostics);

            checkMethods(context, uri, type, endpointPathVars, diagnostics);
        }

        return diagnostics;
    }

    // -----------------------------------------------------------------------
    // Combined single-pass method check
    // -----------------------------------------------------------------------

    /**
     * Iterates {@code type.getMethods()} and performs all per-method
     * diagnostic checks:
     * <ul>
     * <li>Duplicate lifecycle annotation ({@code @OnOpen}, {@code @OnClose}, {@code @OnError})</li>
     * <li>Invalid parameter types for {@code @OnOpen} / {@code @OnClose} methods</li>
     * <li>{@code @PathParam} value mismatch against the endpoint URI</li>
     * <li>Duplicate {@code @OnMessage} handler for the same message format</li>
     * </ul>
     */
    private void checkMethods(JavaDiagnosticsContext context, String uri, IType type,
                              List<String> endpointPathVars, List<Diagnostic> diagnostics) throws JavaModelException {

        // State shared across all methods for duplicate-lifecycle and duplicate-format checks.
        Set<String> visitedLifecycleAnnotations = new HashSet<>();
        Map<Constants.MESSAGE_FORMAT, IAnnotation> seenOnMessageFormats = new HashMap<>();

        for (IMethod method : type.getMethods()) {

            // ---- 1. Resolve which WebSocket annotation (if any) is on this method ----
            Set<String> specialParamTypes = null;
            Set<String> rawSpecialParamTypes = null;
            ErrorCode paramErrorCode = null;
            String lifecycleAnnotName = null;
            IAnnotation onMessageAnnotation = null;

            for (IAnnotation annotation : method.getAnnotations()) {
                String name = annotation.getElementName();

                String matchedFQN = DiagnosticUtils.getMatchedJavaElementName(type, name, Constants.LIFECYCLE_ANNOTATIONS);
                if (matchedFQN == null) {
                    continue; // not a WebSocket annotation we care about
                }
                switch (matchedFQN) {
                    case Constants.ON_OPEN:
                        specialParamTypes = Constants.ON_OPEN_PARAM_OPT_TYPES;
                        rawSpecialParamTypes = Constants.RAW_ON_OPEN_PARAM_OPT_TYPES;
                        paramErrorCode = ErrorCode.InvalidOnOpenParams;
                        lifecycleAnnotName = name;
                        break;
                    case Constants.ON_CLOSE:
                        specialParamTypes = Constants.ON_CLOSE_PARAM_OPT_TYPES;
                        rawSpecialParamTypes = Constants.RAW_ON_CLOSE_PARAM_OPT_TYPES;
                        paramErrorCode = ErrorCode.InvalidOnCloseParams;
                        lifecycleAnnotName = name;
                        break;
                    case Constants.ON_ERROR:
                        lifecycleAnnotName = name;
                        break;
                    case Constants.ON_MESSAGE:
                        onMessageAnnotation = annotation;
                        continue; // @OnMessage is not a lifecycle annotation — skip the duplicate-lifecycle check below
                }

                // ---- 2. Duplicate lifecycle annotation check (@OnOpen/@OnClose/@OnError) ----
                if (visitedLifecycleAnnotations.contains(lifecycleAnnotName)) {
                    JsonArray data = new JsonArray();
                    data.add(Constants.WEBSOCKET_ANNOTATION_FQN.get(lifecycleAnnotName));
                    Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage(ErrorCode.DuplicateLifeCycleAnnotation.getCode(), lifecycleAnnotName),
                                                             range, Constants.DIAGNOSTIC_SOURCE, data,
                                                             ErrorCode.DuplicateLifeCycleAnnotation, DiagnosticSeverity.Error));
                } else {
                    visitedLifecycleAnnotations.add(lifecycleAnnotName);
                }
            }

            // ---- 3. Parameter checks — one pass over params covers both @OnOpen/@OnClose
            //         validation and @PathParam mismatch. @OnMessage format check also runs here. ----
            for (ILocalVariable param : method.getParameters()) {

                // @OnOpen / @OnClose: validate parameter types
                if (paramErrorCode != null) {
                    checkParam(context, uri, type, param, specialParamTypes, rawSpecialParamTypes,
                               paramErrorCode, lifecycleAnnotName, diagnostics);
                }

                // @PathParam mismatch (applies to all methods when endpoint URI vars are known)
                if (endpointPathVars != null) {
                    checkPathParamMismatch(context, uri, type, param, endpointPathVars, diagnostics);
                }

                // @OnMessage: duplicate format check (skip params that are @PathParam)
                if (onMessageAnnotation != null && !hasPathParamAnnotation(type, param)) {
                    checkOnMessageFormat(context, uri, type, param, onMessageAnnotation,
                                         seenOnMessageFormats, diagnostics);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Per-parameter helpers (called from the combined method pass)
    // -----------------------------------------------------------------------

    /**
     * Validates one parameter of an {@code @OnOpen} or {@code @OnClose} method:
     * must be a permitted special type, a primitive/wrapper, or carry {@code @PathParam}.
     */
    private void checkParam(JavaDiagnosticsContext context, String uri, IType type,
                            ILocalVariable param, Set<String> specialParamTypes,
                            Set<String> rawSpecialParamTypes, ErrorCode errorCode,
                            String annotationName, List<Diagnostic> diagnostics) throws JavaModelException {

        String resolvedTypeName = JDTTypeUtils.getResolvedTypeName(param);
        String formatSignature = param.getTypeSignature().replace("/", ".");
        boolean isPrimitive = JavaModelUtil.isPrimitive(formatSignature);
        boolean isSpecialType;
        boolean isPrimWrapped;

        if (resolvedTypeName != null) {
            isSpecialType = specialParamTypes.contains(resolvedTypeName);
            isPrimWrapped = isWrapper(resolvedTypeName);
        } else {
            String simpleParamType = Signature.getSignatureSimpleName(param.getTypeSignature());
            isSpecialType = rawSpecialParamTypes.contains(simpleParamType);
            isPrimWrapped = isWrapper(simpleParamType);
        }

        if (!(isSpecialType || isPrimWrapped || isPrimitive)) {
            Range range = PositionUtils.toNameRange(param, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri,
                                                     createParamTypeDiagMsg(specialParamTypes, annotationName), range,
                                                     Constants.DIAGNOSTIC_SOURCE, null, errorCode, DiagnosticSeverity.Error));
            return; // type already wrong — no need to check @PathParam
        }

        if (!isSpecialType && !hasPathParamAnnotation(type, param)) {
            Range range = PositionUtils.toNameRange(param, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri,
                                                     Messages.getMessage("PathParamsAnnotationMissing"), range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.PathParamsMissingFromParam, DiagnosticSeverity.Error));
        }
    }

    /**
     * For a single parameter that carries {@code @PathParam}, checks that the
     * annotation's value matches a URI variable declared in the endpoint URI.
     */
    private void checkPathParamMismatch(JavaDiagnosticsContext context, String uri, IType type,
                                        ILocalVariable param, List<String> endpointPathVars,
                                        List<Diagnostic> diagnostics) throws JavaModelException {
        for (IAnnotation annotation : param.getAnnotations()) {
            if (!DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                      Constants.PATHPARAM_ANNOTATION)) {
                continue;
            }
            String pathValue = DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.ANNOTATION_VALUE, String.class);
            if (pathValue != null && !endpointPathVars.contains(pathValue)) {
                Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("PathParamWarning"), range,
                                                         Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.PathParamDoesNotMatchEndpointURI, DiagnosticSeverity.Warning));
            }
        }
    }

    /**
     * Checks whether a second {@code @OnMessage} method handles the same message
     * format (TEXT / BINARY / PONG) as a previously seen one.
     *
     * @param seenOnMessageFormats map tracking the first annotation per format,
     *            shared across all methods
     */
    private void checkOnMessageFormat(JavaDiagnosticsContext context, String uri, IType type,
                                      ILocalVariable param, IAnnotation onMessageAnnotation,
                                      Map<Constants.MESSAGE_FORMAT, IAnnotation> seenOnMessageFormats,
                                      List<Diagnostic> diagnostics) throws JavaModelException {

        String resolvedTypeName = JDTTypeUtils.getResolvedTypeName(param);
        String lookupName = resolvedTypeName != null ? resolvedTypeName : Signature.getSignatureSimpleName(param.getTypeSignature());

        Constants.MESSAGE_FORMAT format = MESSAGE_FORMAT_MAP.get(lookupName);
        if (format == null) {
            return; // not a message-type parameter
        }

        IAnnotation previous = seenOnMessageFormats.put(format, onMessageAnnotation);
        if (previous != null) {
            // Both the new and the previously seen @OnMessage get flagged.
            addOnMessageDuplicateDiagnostic(context, uri, onMessageAnnotation, diagnostics);
            addOnMessageDuplicateDiagnostic(context, uri, previous, diagnostics);
        }
    }

    // -----------------------------------------------------------------------
    // Class-level annotation pass (single loop over type.getAnnotations())
    // -----------------------------------------------------------------------

    /**
     * Iterates {@code type.getAnnotations()} and performs all class-level
     * annotation checks:
     * <ul>
     * <li>Validates {@code @ServerEndpoint} URI path (no slash, relative paths,
     * level-1 template, duplicate variables)</li>
     * <li>Extracts URI path-variable names for later {@code @PathParam} mismatch checking</li>
     * </ul>
     *
     * @return list of URI path-variable names, or {@code null} if no endpoint annotation
     *         with a string {@code value} is found
     */
    private List<String> checkTypeAnnotations(JavaDiagnosticsContext context, String uri, IType type,
                                              List<Diagnostic> diagnostics) throws JavaModelException {
        String[] endpointAnnotations = { Constants.SERVER_ENDPOINT_ANNOTATION, Constants.CLIENT_ENDPOINT_ANNOTATION };
        List<String> endpointPathVars = null;

        for (IAnnotation annotation : type.getAnnotations()) {
            String name = annotation.getElementName();

            // Determine which (if any) endpoint annotation this is — single resolution call.
            String matched = DiagnosticUtils.getMatchedJavaElementName(type, name, endpointAnnotations);
            if (matched == null) {
                continue;
            }
            boolean isServerEndpoint = Constants.SERVER_ENDPOINT_ANNOTATION.equals(matched);

            String path = getAnnotationStringValue(annotation);
            if (path == null) {
                continue;
            }

            // Extract URI path-variables from the first endpoint annotation that has a value.
            // Applies to both @ServerEndpoint and @ClientEndpoint.
            if (endpointPathVars == null) {
                endpointPathVars = new ArrayList<>();
                for (String part : path.split(Constants.URI_SEPARATOR)) {
                    if (part.startsWith(Constants.CURLY_BRACE_START) && part.endsWith(Constants.CURLY_BRACE_END)) {
                        endpointPathVars.add(part.substring(1, part.length() - 1));
                    }
                }
            }

            // @ServerEndpoint-only URI validation diagnostics.
            if (isServerEndpoint) {
                Range range = PositionUtils.toNameRange(annotation, context.getUtils());

                if (!DiagnosticUtils.hasLeadingSlash(path)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ServerEndpointNoSlash"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidEndpointPathWithNoStartingSlash, DiagnosticSeverity.Error));
                }
                if (hasRelativePathURIs(path)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ServerEndpointRelative"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidEndpointPathWithRelativePaths, DiagnosticSeverity.Error));
                } else if (!DiagnosticUtils.isValidLevel1URI(path)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ServerEndpointNotLevel1"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidEndpointPathNotTempleateOrPartialURI, DiagnosticSeverity.Error));
                }
                if (hasDuplicateURIVariables(path)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ServerEndpointDuplicateVar"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidEndpointPathDuplicateVariable, DiagnosticSeverity.Error));
                }
            }
        }

        return endpointPathVars;
    }

    private void publicNoArgsConstructorCheck(JavaDiagnosticsContext context, String uri, IType type,
                                              List<Diagnostic> diagnostics) throws JavaModelException {
        ConstructorInfoDiagnosticHelper constructorInfo = ConstructorInfoDiagnosticHelper.getConstructorInfo(type);
        if (constructorInfo.hasParameterizedConstructor() && !constructorInfo.hasValidPublicNoArgsConstructor()) {
            Range range = PositionUtils.toNameRange(type, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri,
                                                     Messages.getMessage("publicNoArgConstructorMissing", type.getElementName()), range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.missingPublicNoArgConstructor, DiagnosticSeverity.Error));
        }
    }

    /**
     * Returns the string {@code value} member of {@code annotation}, or {@code null}.
     */
    private String getAnnotationStringValue(IAnnotation annotation) throws JavaModelException {
        return DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.ANNOTATION_VALUE, String.class);
    }

    // -----------------------------------------------------------------------
    // Predicate helpers
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} when {@code type} carries a WebSocket endpoint annotation
     * ({@code @ServerEndpoint} or {@code @ClientEndpoint}).
     */
    private boolean isWSAnnotatedEndpoint(IType type) throws JavaModelException {
        if (!type.isClass()) {
            return false;
        }
        String[] annotationNames = Stream.of(type.getAnnotations()).map(IAnnotation::getElementName).toArray(String[]::new);
        return !DiagnosticUtils.getMatchedJavaElementNames(type, annotationNames,
                                                           Constants.WS_ANNOTATION_CLASS).isEmpty();
    }

    /**
     * Returns {@code true} if any annotation on {@code param} matches
     * {@code @PathParam}. Any {@link JavaModelException} thrown during matching is
     * logged and treated as a non-match.
     */
    private boolean hasPathParamAnnotation(IType type, ILocalVariable param) throws JavaModelException {
        return Stream.of(param.getAnnotations()).anyMatch(annot -> {
            try {
                return DiagnosticUtils.isMatchedJavaElement(type, annot.getElementName(),
                                                            Constants.PATH_PARAM_ANNOTATION);
            } catch (JavaModelException e) {
                JakartaCorePlugin.logException("Failed to get matched annotation", e);
                return false;
            }
        });
    }

    /**
     * Check if valueClass is a wrapper object for a primitive value. Based on
     * https://github.com/eclipse/lsp4mp/blob/9789a1a996811fade43029605c014c7825e8f1da/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/core/utils/JDTTypeUtils.java#L294-L298
     *
     * @param valueClass the resolved type of valueClass in string or the simple type
     * @return if valueClass is a wrapper object
     */
    private boolean isWrapper(String valueClass) {
        return Constants.WRAPPER_OBJS.contains(valueClass)
               || Constants.RAW_WRAPPER_OBJS.contains(valueClass);
    }

    // -----------------------------------------------------------------------
    // Diagnostic creation helpers
    // -----------------------------------------------------------------------

    /** Emits one {@link ErrorCode#OnMessageDuplicateMethod} diagnostic for {@code annotation}. */
    private void addOnMessageDuplicateDiagnostic(JavaDiagnosticsContext context, String uri,
                                                 IAnnotation annotation, List<Diagnostic> diagnostics) throws JavaModelException {
        Range range = PositionUtils.toNameRange(annotation, context.getUtils());
        diagnostics.add(context.createDiagnostic(uri,
                                                 Messages.getMessage("OnMessageDuplicateMethod"), range,
                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                 ErrorCode.OnMessageDuplicateMethod, DiagnosticSeverity.Error));
    }

    private String createParamTypeDiagMsg(Set<String> methodParamOptTypes, String methodAnnotTarget) {
        String paramMessage = String.join("\n- ", methodParamOptTypes);
        return Messages.getMessage("WebSocketParamType", "@" + methodAnnotTarget, paramMessage);
    }

    // -----------------------------------------------------------------------
    // URI validation helpers
    // -----------------------------------------------------------------------

    /**
     * Check if a URI string contains any sequence with //, /./, or /../
     *
     * @param uriString ServerEndpoint URI
     * @return if a URI has a relative path
     */
    private boolean hasRelativePathURIs(String uriString) {
        return uriString.matches(Constants.REGEX_RELATIVE_PATHS);
    }

    /**
     * Check if a URI string has a duplicate variable.
     *
     * @param uriString ServerEndpoint URI
     * @return if a URI has duplicate variables
     */
    private boolean hasDuplicateURIVariables(String uriString) {
        List<String> vars = Arrays.stream(uriString.split(Constants.URI_SEPARATOR)).filter(s -> s.matches(Constants.REGEX_URI_VARIABLE)).map(s -> s.substring(1, s.length()
                                                                                                                                                                 - 1)).collect(Collectors.toList());
        return vars.size() != new HashSet<>(vars).size();
    }
}
