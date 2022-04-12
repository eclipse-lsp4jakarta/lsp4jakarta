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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.DiagnosticSeverity;

public class WebSocketConstants {
    public static final String DIAGNOSTIC_SOURCE = "jakarta-websocket";

    public static final DiagnosticSeverity ERROR = DiagnosticSeverity.Error;
    public static final DiagnosticSeverity WARNING = DiagnosticSeverity.Warning;

    public static final String DIAGNOSTIC_ERR_MSG = "Cannot calculate WebSocket diagnostics";

    public static final String PATHPARAM_ANNOTATION = "PathParam";
    public static final String PATHPARAM_VALUE_WARN_MSG = "PathParam value does not match specified Endpoint URI";

    public static final String PATHPARAM_DIAGNOSTIC_CODE = "ChangePathParamValue";

    public static final String ANNOTATION_VALUE = "value";

    public static final String URI_SEPARATOR = "/";
    public static final String CURLY_BRACE_START = "{";
    public static final String CURLY_BRACE_END = "}";

    public static final String DIAGNOSTIC_PATH_PARAMS_ANNOT_MISSING = "Parameters of type String, any Java primitive type, or boxed version thereof must be annotated with @PathParams.";
    public static final String DIAGNOSTIC_MAND_PARAMS_MISSING = "Mandatory parameters missing. %s requires to include mandatory parameters of type: \n- %s\n";
    public static final String DIAGNOSTIC_DUP_PARAMS_TYPES = "Duplicate parameter types not allowed. %s requires to include only one parameter of type: \n- %s\n";
    
    /* Diagnostic codes */
    public static final String DIAGNOSTIC_CODE_PATH_PARMS_ANNOT = "AddPathParamsAnnotation";
    public static final String DIAGNOSTIC_CODE_ON_OPEN_INVALID_PARAMS = "OnOpenChangeInvalidParam";
    public static final String DIAGNOSTIC_CODE_ON_CLOSE_INVALID_PARAMS = "OnCloseChangeInvalidParam";
    public static final String DIAGNOSTIC_CODE_ON_ERROR_INVALID_PARAMS = "OnErrorChangeInvalidParam";
    public static final String DIAGNOSTIC_CODE_DUP_PARAMS_TYPES = "DuplicateParamsTypes";

    public static final String DIAGNOSTIC_CODE_ON_ERROR_MAND_PARAMS_MISS = "OnErrorMandatoryParamMissing";
    
    public static final String DIAGNOSTIC_SERVER_ENDPOINT_NO_SLASH = "Server endpoint paths must start with a leading '/'.";
    public static final String DIAGNOSTIC_SERVER_ENDPOINT_NOT_LEVEL1 = "Server endpoint paths must be a URI-template (level-1) or a partial URI.";
    public static final String DIAGNOSTIC_SERVER_ENDPOINT_RELATIVE = "Server endpoint paths must not contain the sequences '/../', '/./' or '//'.";
    public static final String DIAGNOSTIC_SERVER_ENDPOINT_DUPLICATE_VAR = "Server endpoint paths must not use the same variable more than once in a path.";
    public static final String DIAGNOSTIC_SERVER_ENDPOINT= "ChangeInvalidServerEndpoint";

    /* https://jakarta.ee/specifications/websocket/2.0/websocket-spec-2.0.html#applications */
    // Class Level Annotations
    public static final String SERVER_ENDPOINT_ANNOTATION = "ServerEndpoint";
    public static final String CLIENT_ENDPOINT_ANNOTATION = "ClientEndpoint";

    // Superclass
    public static final String ENDPOINT_SUPERCLASS = "Endpoint";
    public static final String IS_SUPERCLASS = "isSuperclass";

    public static final Set<String> WS_ANNOTATION_CLASS = new HashSet<>(Arrays.asList(SERVER_ENDPOINT_ANNOTATION, CLIENT_ENDPOINT_ANNOTATION));

    /* Annotations */
    public static final String ON_OPEN = "OnOpen";
    public static final String ON_CLOSE = "OnClose";
    public static final String ON_ERROR = "OnError";

    public static final String IS_ANNOTATION = "isAnnotation";

    /* Types */
    public static final String PATH_PARAM_ANNOTATION = "PathParam";

    // For OnOpen annotation    
    public static final Set<String> ON_OPEN_PARAM_OPT_TYPES= new HashSet<>(Arrays.asList("jakarta.websocket.EndpointConfig", "jakarta.websocket.Session"));
    public static final Set<String> RAW_ON_OPEN_PARAM_OPT_TYPES= new HashSet<>(Arrays.asList("EndpointConfig", "Session"));

    // For OnClose annotation
    public static final Set<String> ON_CLOSE_PARAM_OPT_TYPES = new HashSet<>(Arrays.asList("jakarta.websocket.CloseReason", "jakarta.websocket.Session"));
    public static final Set<String> RAW_ON_CLOSE_PARAM_OPT_TYPES = new HashSet<>(Arrays.asList("CloseReason", "Session"));

    // For OnError annotation
    // optional onError parameters
    public static final Set<String> ON_ERROR_PARAM_OPT_TYPES = new HashSet<>(Arrays.asList("jakarta.websocket.Session"));
    public static final Set<String> RAW_ON_ERROR_PARAM_OPT_TYPES = new HashSet<>(Arrays.asList("Session"));

    // mandatory onError parameters
    public static final Set<String> ON_ERROR_PARAM_MAND_TYPES = new HashSet<>(Arrays.asList("java.lang.Throwable"));
    public static final Set<String> RAW_ON_ERROR_PARAM_MAND_TYPES = new HashSet<>(Arrays.asList("Throwable"));
    
    public static final Set<String> RAW_WRAPPER_OBJS = new HashSet<>(Arrays.asList("String", "Boolean", "Integer", "Long", "Double", "Float"));
    public static final Set<String> WRAPPER_OBJS = RAW_WRAPPER_OBJS.stream().map(raw -> "java.lang.".concat(raw)).collect(Collectors.toSet());

    // Messages
    public static final String PARAM_TYPE_DIAG_MSG = "Invalid parameter type. When using %s, parameter must be of type: \n- %s\n- annotated with @PathParams and of type String or any Java primitive type or boxed version thereof";

    /* Regex */
    // Check for any URI strings that contain //, /./, or /../
    public static final String REGEX_RELATIVE_PATHS = ".*\\/\\.{0,2}\\/.*";
    // Check that a URI string is a valid level 1 variable (wrapped in curly brackets): alpha-numeric characters, dash, or a percent encoded character
    public static final String REGEX_URI_VARIABLE = "\\{(\\w|-|%20|%21|%23|%24|%25|%26|%27|%28|%29|%2A|%2B|%2C|%2F|%3A|%3B|%3D|%3F|%40|%5B|%5D)+\\}";
}
