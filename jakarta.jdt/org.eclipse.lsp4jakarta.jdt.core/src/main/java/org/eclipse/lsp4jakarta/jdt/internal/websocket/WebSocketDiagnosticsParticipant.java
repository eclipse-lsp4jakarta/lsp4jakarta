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
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
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
 * WebSocket Diagnostic participant.
 */
public class WebSocketDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

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
        HashMap<String, Boolean> checkWSEnd = null;

        alltypes = unit.getAllTypes();
        for (IType type : alltypes) {
            checkWSEnd = isWSEndpoint(type);
            // checks if the class uses annotation to create a WebSocket endpoint
            if (checkWSEnd.get(Constants.IS_ANNOTATION)) {
                // WebSocket Invalid Parameters Diagnostic
                invalidParamsCheck(context, uri, type, unit, diagnostics);

                /* @PathParam Value Mismatch Warning */
                List<String> endpointPathVars = findAndProcessEndpointURI(type);
                /*
                 * WebSocket endpoint annotations must be attached to a class, and thus is
                 * guaranteed to be processed before any of the member method annotations
                 */
                if (endpointPathVars != null) {
                    // PathParam URI Mismatch Warning Diagnostic
                    uriMismatchWarningCheck(context, uri, type, endpointPathVars, diagnostics, unit);
                }

                // OnMessage validation for WebSocket message formats
                onMessageWSMessageFormats(context, uri, type, diagnostics, unit);

                // ServerEndpoint annotation diagnostics
                serverEndpointErrorCheck(context, uri, type, diagnostics, unit);

                publicNoArgsConstructorCheck(context, uri, type, diagnostics);
            }
        }

        return diagnostics;
    }

    private void invalidParamsCheck(JavaDiagnosticsContext context, String uri, IType type, ICompilationUnit unit,
                                    List<Diagnostic> diagnostics) throws JavaModelException {
        IMethod[] allMethods = type.getMethods();
        for (IMethod method : allMethods) {
            IAnnotation[] allAnnotations = method.getAnnotations();
            Set<String> specialParamTypes = null, rawSpecialParamTypes = null;

            for (IAnnotation annotation : allAnnotations) {
                String annotationName = annotation.getElementName();
                ErrorCode diagnosticErrorCode = null;

                if (DiagnosticUtils.isMatchedJavaElement(type, annotationName, Constants.ON_OPEN)) {
                    specialParamTypes = Constants.ON_OPEN_PARAM_OPT_TYPES;
                    rawSpecialParamTypes = Constants.RAW_ON_OPEN_PARAM_OPT_TYPES;
                    diagnosticErrorCode = ErrorCode.InvalidOnOpenParams;
                } else if (DiagnosticUtils.isMatchedJavaElement(type, annotationName, Constants.ON_CLOSE)) {
                    specialParamTypes = Constants.ON_CLOSE_PARAM_OPT_TYPES;
                    rawSpecialParamTypes = Constants.RAW_ON_CLOSE_PARAM_OPT_TYPES;
                    diagnosticErrorCode = ErrorCode.InvalidOnCloseParams;
                }
                if (diagnosticErrorCode != null) {
                    ILocalVariable[] allParams = method.getParameters();
                    for (ILocalVariable param : allParams) {
                        String signature = param.getTypeSignature();
                        String formatSignature = signature.replace("/", ".");
                        String resolvedTypeName = JavaModelUtil.getResolvedTypeName(formatSignature, type);
                        boolean isPrimitive = JavaModelUtil.isPrimitive(formatSignature);
                        boolean isSpecialType;
                        boolean isPrimWrapped;

                        if (resolvedTypeName != null) {
                            isSpecialType = specialParamTypes.contains(resolvedTypeName);
                            isPrimWrapped = isWrapper(resolvedTypeName);
                        } else {
                            String simpleParamType = Signature.getSignatureSimpleName(signature);
                            isSpecialType = rawSpecialParamTypes.contains(simpleParamType);
                            isPrimWrapped = isWrapper(simpleParamType);
                        }

                        // check parameters valid types
                        if (!(isSpecialType || isPrimWrapped || isPrimitive)) {
                            Range range = PositionUtils.toNameRange(param, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     createParamTypeDiagMsg(specialParamTypes, annotationName), range,
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     diagnosticErrorCode, DiagnosticSeverity.Error));
                            continue;
                        }

                        if (!isSpecialType) {
                            // check that if parameter is not a specialType, it has a @PathParam annotation
                            IAnnotation[] param_annotations = param.getAnnotations();
                            boolean hasPathParamAnnot = Arrays.asList(param_annotations).stream().anyMatch(annot -> {
                                try {
                                    return DiagnosticUtils.isMatchedJavaElement(type, annot.getElementName(),
                                                                                Constants.PATH_PARAM_ANNOTATION);
                                } catch (JavaModelException e) {
                                    JakartaCorePlugin.logException("Failed to get matched annotation", e);
                                    return false;
                                }
                            });
                            if (!hasPathParamAnnot) {
                                Range range = PositionUtils.toNameRange(param, context.getUtils());
                                diagnostics.add(context.createDiagnostic(uri,
                                                                         Messages.getMessage("PathParamsAnnotationMissing"), range,
                                                                         Constants.DIAGNOSTIC_SOURCE, null,
                                                                         ErrorCode.PathParamsMissingFromParam, DiagnosticSeverity.Error));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a warning diagnostic if a PathParam annotation does not match any
     * variable parameters of the WebSocket EndPoint URI associated with the class
     * in which the method is contained
     *
     * @param type representing the class list of diagnostics for this class
     *            compilation unit with which the type is associated
     */
    private void uriMismatchWarningCheck(JavaDiagnosticsContext context, String uri, IType type,
                                         List<String> endpointPathVars, List<Diagnostic> diagnostics,
                                         ICompilationUnit unit) throws JavaModelException {
        IMethod[] typeMethods = type.getMethods();
        for (IMethod method : typeMethods) {
            ILocalVariable[] methodParams = method.getParameters();
            for (ILocalVariable param : methodParams) {
                IAnnotation[] paramAnnotations = param.getAnnotations();
                for (IAnnotation annotation : paramAnnotations) {
                    if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                             Constants.PATHPARAM_ANNOTATION)) {
                        IMemberValuePair[] valuePairs = annotation.getMemberValuePairs();
                        for (IMemberValuePair pair : valuePairs) {
                            if (pair.getMemberName().equals(Constants.ANNOTATION_VALUE)
                                && pair.getValueKind() == IMemberValuePair.K_STRING) {
                                String pathValue = (String) pair.getValue();
                                if (!endpointPathVars.contains(pathValue)) {
                                    Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                                    diagnostics.add(context.createDiagnostic(uri,
                                                                             Messages.getMessage("PathParamWarning"), range,
                                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                                             ErrorCode.PathParamDoesNotMatchEndpointURI, DiagnosticSeverity.Warning));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates an error diagnostic if there exists more than one method annotated
     * with @OnMessage for a given message format.
     *
     * @param type
     * @param diagnostics
     * @param unit
     * @throws JavaModel
     */
    private void onMessageWSMessageFormats(JavaDiagnosticsContext context, String uri, IType type,
                                           List<Diagnostic> diagnostics, ICompilationUnit unit) throws JavaModelException {
        IMethod[] typeMethods = type.getMethods();
        IAnnotation onMessageTextUsed = null;
        IAnnotation onMessageBinaryUsed = null;
        IAnnotation onMessagePongUsed = null;
        for (IMethod method : typeMethods) {
            IAnnotation[] allAnnotations = method.getAnnotations();
            for (IAnnotation annotation : allAnnotations) {
                if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), Constants.ON_MESSAGE)) {
                    ILocalVariable[] allParams = method.getParameters();
                    for (ILocalVariable param : allParams) {
                        if (!isParamPath(type, param)) {
                            String signature = param.getTypeSignature();
                            String formatSignature = signature.replace("/", ".");
                            String resolvedTypeName = JavaModelUtil.getResolvedTypeName(formatSignature, type);
                            String typeName = null;
                            if (resolvedTypeName == null) {
                                typeName = Signature.getSignatureSimpleName(signature);
                            }
                            if ((resolvedTypeName != null
                                 && Constants.LONG_MESSAGE_CLASSES.contains(resolvedTypeName))
                                || Constants.SHORT_MESSAGE_CLASSES.contains(typeName)) {
                                Constants.MESSAGE_FORMAT messageFormat = resolvedTypeName != null ? getMessageFormat(resolvedTypeName, true) : getMessageFormat(typeName, false);
                                switch (messageFormat) {
                                    case TEXT:
                                        if (onMessageTextUsed != null) {
                                            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                                            diagnostics.add(context.createDiagnostic(uri,
                                                                                     Messages.getMessage("OnMessageDuplicateMethod"), range,
                                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                                     ErrorCode.OnMessageDuplicateMethod,
                                                                                     DiagnosticSeverity.Error));

                                            range = PositionUtils.toNameRange(onMessageTextUsed, context.getUtils());
                                            diagnostics.add(context.createDiagnostic(uri,
                                                                                     Messages.getMessage("OnMessageDuplicateMethod"), range,
                                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                                     ErrorCode.OnMessageDuplicateMethod,
                                                                                     DiagnosticSeverity.Error));
                                        }
                                        onMessageTextUsed = annotation;
                                        break;
                                    case BINARY:
                                        if (onMessageBinaryUsed != null) {
                                            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                                            diagnostics.add(context.createDiagnostic(uri,
                                                                                     Messages.getMessage("OnMessageDuplicateMethod"), range,
                                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                                     ErrorCode.OnMessageDuplicateMethod,
                                                                                     DiagnosticSeverity.Error));

                                            range = PositionUtils.toNameRange(onMessageBinaryUsed, context.getUtils());
                                            diagnostics.add(context.createDiagnostic(uri,
                                                                                     Messages.getMessage("OnMessageDuplicateMethod"), range,
                                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                                     ErrorCode.OnMessageDuplicateMethod,
                                                                                     DiagnosticSeverity.Error));

                                        }
                                        onMessageBinaryUsed = annotation;
                                        break;
                                    case PONG:
                                        if (onMessagePongUsed != null) {
                                            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                                            diagnostics.add(context.createDiagnostic(uri,
                                                                                     Messages.getMessage("OnMessageDuplicateMethod"), range,
                                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                                     ErrorCode.OnMessageDuplicateMethod,
                                                                                     DiagnosticSeverity.Error));

                                            range = PositionUtils.toNameRange(onMessagePongUsed, context.getUtils());
                                            diagnostics.add(context.createDiagnostic(uri,
                                                                                     Messages.getMessage("OnMessageDuplicateMethod"), range,
                                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                                     ErrorCode.OnMessageDuplicateMethod,
                                                                                     DiagnosticSeverity.Error));
                                        }
                                        onMessagePongUsed = annotation;
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create an error diagnostic if a ServerEndpoint annotation's URI contains
     * relative
     * paths, missing a leading slash, or does not follow a valid level-1 template
     * URI.
     */
    private void serverEndpointErrorCheck(JavaDiagnosticsContext context, String uri, IType type,
                                          List<Diagnostic> diagnostics, ICompilationUnit unit) throws JavaModelException {
        IAnnotation[] annotations = type.getAnnotations();
        for (IAnnotation annotation : annotations) {
            if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                     Constants.SERVER_ENDPOINT_ANNOTATION)) {
                for (IMemberValuePair annotationMemberValuePair : annotation.getMemberValuePairs()) {
                    if (annotationMemberValuePair.getMemberName().equals(Constants.ANNOTATION_VALUE)) {
                        String path = annotationMemberValuePair.getValue().toString();
                        if (!DiagnosticUtils.hasLeadingSlash(path)) {
                            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("ServerEndpointNoSlash"), range,
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     ErrorCode.InvalidEndpointPathWithNoStartingSlash,
                                                                     DiagnosticSeverity.Error));
                        }
                        if (hasRelativePathURIs(path)) {
                            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("ServerEndpointRelative"), range,
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     ErrorCode.InvalidEndpointPathWithRelativePaths,
                                                                     DiagnosticSeverity.Error));
                        } else if (!DiagnosticUtils.isValidLevel1URI(path)) {
                            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("ServerEndpointNotLevel1"), range,
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     ErrorCode.InvalidEndpointPathNotTempleateOrPartialURI,
                                                                     DiagnosticSeverity.Error));
                        }
                        if (hasDuplicateURIVariables(path)) {
                            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("ServerEndpointDuplicateVar"), range,
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     ErrorCode.InvalidEndpointPathDuplicateVariable,
                                                                     DiagnosticSeverity.Error));
                        }
                    }
                }
            }
        }
    }

    private void publicNoArgsConstructorCheck(JavaDiagnosticsContext context, String uri, IType type,
                                              List<Diagnostic> diagnostics) throws JavaModelException {

        boolean hasConstructor = false, hasPublicNoArgConstructor = false;

        for (IMethod method : type.getMethods()) {
            if (DiagnosticUtils.isConstructorMethod(method)) {
                hasConstructor = true;
                String[] params = method.getParameterTypes();
                int flags = method.getFlags();
                if (params.length == 0 && Flags.isPublic(flags)) {
                    hasPublicNoArgConstructor = true;
                }
            }
        }

        if (hasConstructor && !hasPublicNoArgConstructor) {
            Range range = PositionUtils.toNameRange(type, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri,
                                                     Messages.getMessage("publicNoArgConstructorMissing", type.getElementName()), range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.missingPublicNoArgConstructor,
                                                     DiagnosticSeverity.Error));
        }

    }

    /**
     * Finds a WebSocket EndPoint annotation and extracts all variable parameters in
     * the EndPoint URI
     *
     * @param type representing the class
     * @return List of variable parameters in the EndPoint URI if one exists, null
     *         otherwise
     */
    private List<String> findAndProcessEndpointURI(IType type) throws JavaModelException {
        String endpointURI = null;
        IAnnotation[] typeAnnotations = type.getAnnotations();
        String[] targetAnnotations = { Constants.SERVER_ENDPOINT_ANNOTATION, Constants.CLIENT_ENDPOINT_ANNOTATION };
        for (IAnnotation annotation : typeAnnotations) {
            String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type, annotation.getElementName(),
                                                                                 targetAnnotations);
            if (matchedAnnotation != null) {
                IMemberValuePair[] valuePairs = annotation.getMemberValuePairs();
                for (IMemberValuePair pair : valuePairs) {
                    if (pair.getMemberName().equals(Constants.ANNOTATION_VALUE)
                        && pair.getValueKind() == IMemberValuePair.K_STRING) {
                        endpointURI = (String) pair.getValue();
                    }
                }
            }
        }
        if (endpointURI == null) {
            return null;
        }
        List<String> endpointPathVars = new ArrayList<String>();
        String[] endpointParts = endpointURI.split(Constants.URI_SEPARATOR);
        for (String part : endpointParts) {
            if (part.startsWith(Constants.CURLY_BRACE_START)
                && part.endsWith(Constants.CURLY_BRACE_END)) {
                endpointPathVars.add(part.substring(1, part.length() - 1));
            }
        }
        return endpointPathVars;
    }

    /**
     * Check if valueClass is a wrapper object for a primitive value Based on
     * https://github.com/eclipse/lsp4mp/blob/9789a1a996811fade43029605c014c7825e8f1da/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/core/utils/JDTTypeUtils.java#L294-L298
     *
     * @param valueClass the resolved type of valueClass in string or the simple
     *            type of valueClass
     * @return if valueClass is a wrapper object
     */
    private boolean isWrapper(String valueClass) {
        return Constants.WRAPPER_OBJS.contains(valueClass)
               || Constants.RAW_WRAPPER_OBJS.contains(valueClass);
    }

    /**
     * Checks if type is a WebSocket endpoint by meeting one of the 2 conditions
     * listed on
     * https://jakarta.ee/specifications/websocket/2.0/websocket-spec-2.0.html#applications
     * are met: class is annotated or class implements Endpoint class
     *
     * @param type the type representing the class
     * @return the conditions for a class to be a WebSocket endpoint
     * @throws JavaModelException
     */
    private HashMap<String, Boolean> isWSEndpoint(IType type) throws JavaModelException {
        HashMap<String, Boolean> wsEndpoint = new HashMap<>();

        // check trivial case
        if (!type.isClass()) {
            wsEndpoint.put(Constants.IS_ANNOTATION, false);
            wsEndpoint.put(Constants.IS_SUPERCLASS, false);
            return wsEndpoint;
        }

        // Check that class follows
        // https://jakarta.ee/specifications/websocket/2.0/websocket-spec-2.0.html#applications
        List<String> endpointAnnotations = DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                                      Stream.of(type.getAnnotations()).map(annotation -> annotation.getElementName()).toArray(String[]::new),
                                                                                      Constants.WS_ANNOTATION_CLASS);

        boolean useSuperclass = false;
        try {
            useSuperclass = TypeHierarchyUtils.doesITypeHaveSuperType(type, Constants.ENDPOINT_SUPERCLASS) >= 0;
        } catch (CoreException e) {
            JakartaCorePlugin.logException(Constants.DIAGNOSTIC_ERR_MSG, e);
        }

        wsEndpoint.put(Constants.IS_ANNOTATION, (endpointAnnotations.size() > 0));
        wsEndpoint.put(Constants.IS_SUPERCLASS, useSuperclass);

        return wsEndpoint;
    }

    private boolean isParamPath(IType type, ILocalVariable param) throws JavaModelException {
        IAnnotation[] allVariableAnnotations = param.getAnnotations();
        for (IAnnotation variableAnnotation : allVariableAnnotations) {
            if (DiagnosticUtils.isMatchedJavaElement(type, variableAnnotation.getElementName(),
                                                     Constants.PATH_PARAM_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    private Constants.MESSAGE_FORMAT getMessageFormat(String typeName, boolean longName) {
        if (longName) {
            switch (typeName) {
                case Constants.STRING_CLASS_LONG:
                    return Constants.MESSAGE_FORMAT.TEXT;
                case Constants.READER_CLASS_LONG:
                    return Constants.MESSAGE_FORMAT.TEXT;
                case Constants.BYTEBUFFER_CLASS_LONG:
                    return Constants.MESSAGE_FORMAT.BINARY;
                case Constants.INPUTSTREAM_CLASS_LONG:
                    return Constants.MESSAGE_FORMAT.BINARY;
                case Constants.PONGMESSAGE_CLASS_LONG:
                    return Constants.MESSAGE_FORMAT.PONG;
                default:
                    throw new IllegalArgumentException("Invalid message format type");
            }
        }
        switch (typeName) {
            case Constants.STRING_CLASS_SHORT:
                return Constants.MESSAGE_FORMAT.TEXT;
            case Constants.READER_CLASS_SHORT:
                return Constants.MESSAGE_FORMAT.TEXT;
            case Constants.BYTEBUFFER_CLASS_SHORT:
                return Constants.MESSAGE_FORMAT.BINARY;
            case Constants.INPUTSTREAM_CLASS_SHORT:
                return Constants.MESSAGE_FORMAT.BINARY;
            case Constants.PONGMESSAGE_CLASS_SHORT:
                return Constants.MESSAGE_FORMAT.PONG;
            default:
                throw new IllegalArgumentException("Invalid message format type");
        }
    }

    private String createParamTypeDiagMsg(Set<String> methodParamOptTypes, String methodAnnotTarget) {
        String paramMessage = String.join("\n- ", methodParamOptTypes);
        return Messages.getMessage("WebSocketParamType", "@" + methodAnnotTarget, paramMessage);
    }

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
     * Check if a URI string has a duplicate variable
     *
     * @param uriString ServerEndpoint URI
     * @return if a URI has duplicate variables
     */
    private boolean hasDuplicateURIVariables(String uriString) {
        HashSet<String> variables = new HashSet<String>();
        for (String segment : uriString.split(Constants.URI_SEPARATOR)) {
            if (segment.matches(Constants.REGEX_URI_VARIABLE)) {
                String variable = segment.substring(1, segment.length() - 1);
                if (variables.contains(variable)) {
                    return true;
                } else {
                    variables.add(variable);
                }
            }
        }
        return false;
    }
}
