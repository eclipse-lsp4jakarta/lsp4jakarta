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
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.core.websocket;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.lsp4j.DiagnosticSeverity;

public class WebSocketConstants {
    public static final String DIAGNOSTIC_SOURCE = "jakarta-websocket";

    public static final DiagnosticSeverity ERROR = DiagnosticSeverity.Error;
    public static final DiagnosticSeverity WARNING = DiagnosticSeverity.Warning;

    public static final String DIAGNOSTIC_ERR_MSG = "Cannot calculate WebSocket diagnostics";

    public static final String DIAGNOSTIC_PATH_PARAMS_ANNOT_MISSING = "Parameter is missing annotation @PathParams.";
    public static final String DIAGNOSTIC_CODE_PATH_PARMS_ANNOT = "AddPathParamsAnnotation";
    
    public static final String DIAGNOSTIC_ON_OPEN_INVALID_PARAMS = "Invalid parameter type. Parameter must be of type Session, EndpointConfig parameter, or if annotated with @PathParam of type String, any Java primitive type or boxed version thereof.";
    public static final String DIAGNOSTIC_CODE_ON_OPEN_INVALID_PARAMS = "ChangeInvalidParam";
    
    /* https://jakarta.ee/specifications/websocket/2.0/websocket-spec-2.0.html#applications */
    // Class Level Annotations
    public static final String SERVER_ENDPOINT_ANNOTATION = "ServerEndpoint";
    public static final String CLIENT_ENDPOINT_ANNOTATION = "ClientEndpoint";
    
    // Superclass
    public static final String ENDPOINT_SUPERCLASS = "Endpoint";
    public static final Set<String> WS_ANNOTATION_CLASS = new HashSet<>(Arrays.asList(SERVER_ENDPOINT_ANNOTATION, CLIENT_ENDPOINT_ANNOTATION));

    /* Annotations */
    public static final String ON_OPEN = "OnOpen";

    /* Types */
    // For OnOpen annotation
    public static final String PATH_PARAM_ANNOTATION = "PathParam";
    
    public final static Set<String> ON_OPEN_PARAM_OPT_TYPES= new HashSet<>(Arrays.asList("jakarta.websocket.EndpointConfig", "jakarta.websocket.Session"));
    
    public final static Set<String> WRAPPER_OBJS = new HashSet<>(Arrays.asList("java.lang.String", "java.lang.Boolean", "java.lang.Integer", "java.lang.Long", "java.lang.Double", "java.lang.Float"));
}