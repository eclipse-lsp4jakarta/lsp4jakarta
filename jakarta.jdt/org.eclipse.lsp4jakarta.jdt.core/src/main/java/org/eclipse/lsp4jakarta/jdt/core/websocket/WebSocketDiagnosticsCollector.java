/******************************************************************************* 
 * Copyright (c) 2022 IBM Corporation and others. 
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

package org.eclipse.lsp4jakarta.jdt.core.websocket;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.DiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.JDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.websocket.WebSocketConstants;
import org.eclipse.jdt.core.*;
import org.eclipse.lsp4jakarta.jdt.core.AnnotationUtil;

import static org.eclipse.lsp4jakarta.jdt.core.TypeHierarchyUtils.doesITypeHaveSuperType;

public class WebSocketDiagnosticsCollector implements DiagnosticsCollector {
    public WebSocketDiagnosticsCollector() {
    }

    private Diagnostic createDiagnostic(IJavaElement el, ICompilationUnit unit, String msg, String code) {
        try {
            ISourceRange nameRange = JDTUtils.getNameRange(el);
            Range range = JDTUtils.toRange(unit, nameRange.getOffset(), nameRange.getLength());
            Diagnostic diagnostic = new Diagnostic(range, msg);
            diagnostic.setCode(code);
            completeDiagnostic(diagnostic);
            return diagnostic;
        } catch (JavaModelException e) {
            JakartaCorePlugin.logException(WebSocketConstants.DIAGNOSTIC_ERR_MSG, e);
        }
        return null;
    }

    @Override
    public void completeDiagnostic(Diagnostic diagnostic) {
        diagnostic.setSource(WebSocketConstants.DIAGNOSTIC_SOURCE);
        diagnostic.setSeverity(WebSocketConstants.ERROR);
    }

    @Override
    public void collectDiagnostics(ICompilationUnit unit, List<Diagnostic> diagnostics) {
        if (unit == null) {
            return;
        }

        IType[] alltypes;

        HashMap<String, Boolean> checkWSEnd = null;

        try {
            alltypes = unit.getAllTypes();
            for (IType type : alltypes) {
                checkWSEnd = isWSEndpoint(type);
                // checks if the class uses annotation to create a WebSocket endpoint
                if (checkWSEnd.get(WebSocketConstants.IS_ANNOTATION)) {
                    // WebSocket Invalid Parameters Diagnostic
                    invalidParamsCheck(type, WebSocketConstants.ON_OPEN, WebSocketConstants.ON_OPEN_PARAM_OPT_TYPES, 
                            WebSocketConstants.RAW_ON_OPEN_PARAM_OPT_TYPES, WebSocketConstants.DIAGNOSTIC_CODE_ON_OPEN_INVALID_PARAMS, 
                            Collections.emptySet(), Collections.emptySet(), unit, diagnostics);
                    invalidParamsCheck(type, WebSocketConstants.ON_CLOSE, WebSocketConstants.ON_CLOSE_PARAM_OPT_TYPES, 
                            WebSocketConstants.RAW_ON_CLOSE_PARAM_OPT_TYPES, WebSocketConstants.DIAGNOSTIC_CODE_ON_CLOSE_INVALID_PARAMS, 
                            Collections.emptySet(), Collections.emptySet(), unit, diagnostics);
                    invalidParamsCheck(type, WebSocketConstants.ON_ERROR, WebSocketConstants.ON_ERROR_PARAM_OPT_TYPES, 
                            WebSocketConstants.RAW_ON_ERROR_PARAM_OPT_TYPES, WebSocketConstants.DIAGNOSTIC_CODE_ON_ERROR_INVALID_PARAMS,
                            WebSocketConstants.ON_ERROR_PARAM_MAND_TYPES, WebSocketConstants.RAW_ON_ERROR_PARAM_MAND_TYPES, unit, diagnostics);
                    
                    // PathParam URI Mismatch Warning Diagnostic
                    uriMismatchWarningCheck(type, diagnostics, unit);
                    // ServerEndpoint annotation diagnostics
                    serverEndpointErrorCheck(type, diagnostics, unit);
                }
            }
        } catch (JavaModelException e) {
            JakartaCorePlugin.logException(WebSocketConstants.DIAGNOSTIC_ERR_MSG, e);
        }
    }

    private void invalidParamsCheck(IType type, String methodAnnotTarget, Set<String> specialParamTypes, Set<String> rawSpecialParamTypes, String diagnosticCode,
            Set<String> mandParamTypes, Set<String> rawMandParamTypes,  ICompilationUnit unit, List<Diagnostic> diagnostics) throws JavaModelException {

        IMethod[] allMethods = type.getMethods();

        for (IMethod method : allMethods) {
            IAnnotation[] allAnnotations = method.getAnnotations();

            for (IAnnotation annotation : allAnnotations) {
                if (annotation.getElementName().equals(methodAnnotTarget)) {
                    ILocalVariable[] allParams = method.getParameters();
                    HashMap<String, ILocalVariable> mandTypeCounter = initializeHashMap(mandParamTypes);
                    HashMap<String, ILocalVariable> rawMandTypeCounter = initializeHashMap(rawMandParamTypes);
                    Boolean isResolvedType = false;

                    for (ILocalVariable param : allParams) {
                        String signature = param.getTypeSignature();
                        String formatSignature = signature.replace("/", ".");
                        String resolvedTypeName = JavaModelUtil.getResolvedTypeName(formatSignature, type);

                        boolean isPrimitive = JavaModelUtil.isPrimitive(formatSignature);
                        boolean isSpecialType;
                        boolean isPrimWrapped;
                        boolean isMandParam;

                        // general paramType 
                        String genParamType;
                        Set<String> genSpecialParamTypes;
                        Set<String> genMandParamTypes;
                        HashMap<String, ILocalVariable> genMandTypeCounter;

                        if (resolvedTypeName != null) {
                            genParamType = resolvedTypeName;
                            genSpecialParamTypes = specialParamTypes;
                            genMandParamTypes = mandParamTypes;
                            isResolvedType = true;
                            genMandTypeCounter = mandTypeCounter;
                        } else {
                            String simpleParamType = Signature.getSignatureSimpleName(signature);
                            genParamType = simpleParamType;
                            genSpecialParamTypes = rawSpecialParamTypes;
                            genMandParamTypes = rawMandParamTypes;
                            genMandTypeCounter = rawMandTypeCounter;
                        }

                        isSpecialType = genSpecialParamTypes.contains(genParamType);
                        isPrimWrapped = isWrapper(genParamType);
                        isMandParam = genMandParamTypes.contains(genParamType);

                        if (isMandParam) {
                            if (genMandTypeCounter.get(genParamType) != null) {
                                String diagMsg = createParamTypeDiagMsg(WebSocketConstants.DIAGNOSTIC_DUP_PARAMS_TYPES, 
                                        methodAnnotTarget, specialParamTypes, mandParamTypes);
                                Diagnostic diagnostic = createDiagnostic(param, unit,
                                        diagMsg, WebSocketConstants.DIAGNOSTIC_CODE_DUP_PARAMS_TYPES);
                                diagnostics.add(diagnostic);

                                Diagnostic diagnostic2 = createDiagnostic(genMandTypeCounter.get(genParamType), unit,
                                        diagMsg, WebSocketConstants.DIAGNOSTIC_CODE_DUP_PARAMS_TYPES);
                                diagnostics.add(diagnostic2);
                                continue;
                            } else {
                                genMandTypeCounter.put(genParamType, param);
                            }
                            continue;
                        }

                        // check parameters valid types
                        if (!(isSpecialType || isPrimWrapped || isPrimitive)) {
                            String diagMessage = createParamTypeDiagMsg(WebSocketConstants.PARAM_TYPE_DIAG_MSG, 
                                    methodAnnotTarget, specialParamTypes, mandParamTypes);
                            Diagnostic diagnostic = createDiagnostic(param, unit,
                                    diagMessage, diagnosticCode);
                            diagnostics.add(diagnostic);
                            continue;
                        }

                        if (!isSpecialType) {
                            // check that if parameter is not a specialType, it has a @PathParam annotation
                            IAnnotation[] param_annotations = param.getAnnotations();
                            boolean hasPathParamAnnot = Arrays.asList(param_annotations).stream().anyMatch(
                                    annot -> annot.getElementName().equals(WebSocketConstants.PATH_PARAM_ANNOTATION));

                            if (!hasPathParamAnnot) {
                                Diagnostic diagnostic = createDiagnostic(param, unit,
                                        WebSocketConstants.DIAGNOSTIC_PATH_PARAMS_ANNOT_MISSING,
                                        WebSocketConstants.DIAGNOSTIC_CODE_PATH_PARMS_ANNOT);
                                diagnostics.add(diagnostic);
                            }
                        }
                    }

                    HashMap<String, ILocalVariable> genMandTypeCounter = isResolvedType ? mandTypeCounter : rawMandTypeCounter;

                    // check that all mandatory parameters are present
                    for (HashMap.Entry<String, ILocalVariable> entry : genMandTypeCounter.entrySet()) {
                        if (entry.getValue() == null) {
                            String diagMessage = createParamTypeDiagMsg(WebSocketConstants.DIAGNOSTIC_MAND_PARAMS_MISSING, 
                                    methodAnnotTarget, Collections.emptySet(), mandParamTypes);
                            Diagnostic diagnostic = createDiagnostic(method, unit,
                                    diagMessage, WebSocketConstants.DIAGNOSTIC_CODE_ON_ERROR_MAND_PARAMS_MISS);
                            diagnostics.add(diagnostic);
                        }
                    }
                }
            }
        }
    }


    /**
     * Creates a hashmap with the given set of keys and initializes the values to null.
     * @param types set of parameter types
     * @return hashmap with keys initialized to types
     */
    private HashMap<String, ILocalVariable> initializeHashMap(Set<String> types) {
        HashMap<String, ILocalVariable> numTypes = new HashMap<>();
        for (String type : types) {
            numTypes.put(type, null);
        }
        return numTypes;
    }

    /**
     * Creates a warning diagnostic if a PathParam annotation does not match any
     * variable parameters of the WebSocket EndPoint URI associated with the class
     * in which the method is contained
     * 
     * @param type representing the class list of diagnostics for this class
     *             compilation unit with which the type is associated
     */
    private void uriMismatchWarningCheck(IType type, List<Diagnostic> diagnostics, ICompilationUnit unit)
            throws JavaModelException {

        /* @PathParam Value Mismatch Warning */
        List<String> endpointPathVars = findAndProcessEndpointURI(type);
        /*
         * WebSocket endpoint annotations must be attached to a class, and thus is
         * guaranteed to be processed before any of the member method annotations
         */
        if (endpointPathVars == null) {
            return;
        }
        IMethod[] typeMethods = type.getMethods();
        for (IMethod method : typeMethods) {
            ILocalVariable[] methodParams = method.getParameters();
            for (ILocalVariable param : methodParams) {
                IAnnotation[] paramAnnotations = param.getAnnotations();
                for (IAnnotation annotation : paramAnnotations) {
                    if (annotation.getElementName() == WebSocketConstants.PATHPARAM_ANNOTATION) {
                        IMemberValuePair[] valuePairs = annotation.getMemberValuePairs();
                        for (IMemberValuePair pair : valuePairs) {
                            if (pair.getMemberName().equals(WebSocketConstants.ANNOTATION_VALUE)
                                    && pair.getValueKind() == IMemberValuePair.K_STRING) {
                                String pathValue = (String) pair.getValue();
                                if (!endpointPathVars.contains(pathValue)) {
                                    Diagnostic d = createPathParamWarningDiagnostic(annotation, unit);
                                    diagnostics.add(d);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create an error diagnostic if a ServerEndpoint annotation does not follow the rules.
     */
    private void serverEndpointErrorCheck(IType type, List<Diagnostic> diagnostics, ICompilationUnit unit) throws JavaModelException {
        for (IAnnotation annotation : type.getAnnotations()) {
            if (annotation.getElementName().equals(WebSocketConstants.SERVER_ENDPOINT_ANNOTATION)) {
                for (IMemberValuePair annotationMemberValuePair : annotation.getMemberValuePairs()) {
                    if (annotationMemberValuePair.getMemberName().equals(WebSocketConstants.ANNOTATION_VALUE)) {
                        String path = annotationMemberValuePair
                                .getValue()
                                .toString();
                        Diagnostic diagnostic;
                        if (!JDTUtils.hasLeadingSlash(path)) {
                            diagnostic = createDiagnostic(annotation, unit,
                                    WebSocketConstants.DIAGNOSTIC_SERVER_ENDPOINT_NO_SLASH,
                                    WebSocketConstants.DIAGNOSTIC_SERVER_ENDPOINT);
                            diagnostics.add(diagnostic);
                        }
                        if (hasRelativePathURIs(path)) {
                            diagnostic = createDiagnostic(annotation, unit,
                                    WebSocketConstants.DIAGNOSTIC_SERVER_ENDPOINT_RELATIVE,
                                    WebSocketConstants.DIAGNOSTIC_SERVER_ENDPOINT);
                            diagnostics.add(diagnostic);
                        } else if (!JDTUtils.isValidLevel1URI(path)) {
                            diagnostic = createDiagnostic(annotation, unit,
                                    WebSocketConstants.DIAGNOSTIC_SERVER_ENDPOINT_NOT_LEVEL1,
                                    WebSocketConstants.DIAGNOSTIC_SERVER_ENDPOINT);
                            diagnostics.add(diagnostic);
                        }
                        if (hasDuplicateURIVariables(path)) {
                            diagnostic = createDiagnostic(annotation, unit,
                                    WebSocketConstants.DIAGNOSTIC_SERVER_ENDPOINT_DUPLICATE_VAR,
                                    WebSocketConstants.DIAGNOSTIC_SERVER_ENDPOINT);
                            diagnostics.add(diagnostic);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a PathParam URI Mismatch Warning Diagnostic given its components
     * 
     * @param the annotation onto which the diagnostic needs to be displayed the
     *            compilation unit with which said annotation is associated
     * @return the final Diagnostic with its attributes set as needed
     */
    private Diagnostic createPathParamWarningDiagnostic(IJavaElement annotation, IOpenable unit)
            throws JavaModelException {
        ISourceRange nameRange = JDTUtils.getNameRange(annotation);
        Range range = JDTUtils.toRange(unit, nameRange.getOffset(), nameRange.getLength());
        Diagnostic diagnostic = new Diagnostic(range, WebSocketConstants.PATHPARAM_VALUE_WARN_MSG);
        diagnostic.setSource(WebSocketConstants.DIAGNOSTIC_SOURCE);
        diagnostic.setSeverity(WebSocketConstants.WARNING);
        diagnostic.setCode(WebSocketConstants.PATHPARAM_DIAGNOSTIC_CODE);
        return diagnostic;
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
        for (IAnnotation annotation : typeAnnotations) {
            if (annotation.getElementName().equals(WebSocketConstants.SERVER_ENDPOINT_ANNOTATION)
                    || annotation.getElementName().equals(WebSocketConstants.CLIENT_ENDPOINT_ANNOTATION)) {
                IMemberValuePair[] valuePairs = annotation.getMemberValuePairs();
                for (IMemberValuePair pair : valuePairs) {
                    if (pair.getMemberName().equals(WebSocketConstants.ANNOTATION_VALUE)
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
        String[] endpointParts = endpointURI.split(WebSocketConstants.URI_SEPARATOR);
        for (String part : endpointParts) {
            if (part.startsWith(WebSocketConstants.CURLY_BRACE_START)
                    && part.endsWith(WebSocketConstants.CURLY_BRACE_END)) {
                endpointPathVars.add(part.substring(1, part.length() - 1));
            }
        }
        return endpointPathVars;
    }

    /**
     * Check if valueClass is a wrapper object for a primitive value. Based on
     * https://github.com/eclipse/lsp4mp/blob/9789a1a996811fade43029605c014c7825e8f1da/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/core/utils/JDTTypeUtils.java#L294-L298
     * 
     * @param valueClass the resolved type of valueClass in string or the simple
     *                   type of valueClass
     * @return if valueClass is a wrapper object
     */
    private boolean isWrapper(String valueClass) {
        return WebSocketConstants.WRAPPER_OBJS.contains(valueClass)
                || WebSocketConstants.RAW_WRAPPER_OBJS.contains(valueClass);
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
            wsEndpoint.put(WebSocketConstants.IS_ANNOTATION, false);
            wsEndpoint.put(WebSocketConstants.IS_SUPERCLASS, false);
            return wsEndpoint;
        }

        // Check that class follows
        // https://jakarta.ee/specifications/websocket/2.0/websocket-spec-2.0.html#applications
        List<String> scopes = AnnotationUtil.getScopeAnnotations(type, WebSocketConstants.WS_ANNOTATION_CLASS);
        boolean useAnnotation = scopes.size() > 0;

        boolean useSuperclass = false;

        String superclass = type.getSuperclassName();
        try {
            int r = doesITypeHaveSuperType(type, WebSocketConstants.ENDPOINT_SUPERCLASS);
            useSuperclass = (r >= 0);
        } catch (CoreException e) {
            JakartaCorePlugin.logException(WebSocketConstants.DIAGNOSTIC_ERR_MSG, e);
        }

        wsEndpoint.put(WebSocketConstants.IS_ANNOTATION, useAnnotation);
        wsEndpoint.put(WebSocketConstants.IS_SUPERCLASS, useSuperclass);

        return wsEndpoint;
    }

    /**
     * Creates a diagnotic message for parameter types that are not supported by
     * @param initialMsg the initial message to be displayed
     * @param methodAnnotTarget the annotation target of the method
     * @param methodParamOptTypes the optional parameter types of the method
     * @param mandParamTypes the mandatory parameter types of the method
     * @return the final diagnostic message
     */
    private String createParamTypeDiagMsg(String initialMsg, String methodAnnotTarget, Set<String> methodParamOptTypes, Set<String> mandParamTypes) {
        String paramMessage = String.join("\n- ", methodParamOptTypes);
        
        if (mandParamTypes.size() > 0) {
            paramMessage += (methodParamOptTypes.size() > 0 ? "\n- " : "") + String.join("\n- ", mandParamTypes);
        }
        return String.format(initialMsg, "@" + methodAnnotTarget, paramMessage);
    }

    /**
     * Check if a URI string contains any sequence with //, /./, or /../
     *
     * @param uriString ServerEndpoint URI
     * @return if a URI has a relative path
     */
    private boolean hasRelativePathURIs(String uriString) {
        return uriString.matches(WebSocketConstants.REGEX_RELATIVE_PATHS);
    }

    /**
     * Check if a URI string has a duplicate variable
     * 
     * @param uriString ServerEndpoint URI
     * @return if a URI has duplicate variables
     */
    private boolean hasDuplicateURIVariables(String uriString) {
        HashSet<String> variables = new HashSet<String>();
        for (String segment : uriString.split(WebSocketConstants.URI_SEPARATOR)) {
            if (segment.matches(WebSocketConstants.REGEX_URI_VARIABLE)) {
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

