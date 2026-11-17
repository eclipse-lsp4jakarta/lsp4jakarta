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
 *     Yijia Jing
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.jsonp;

/**
 * JSON Processing (JSON-P) diagnostic constants.
 */
public class Constants {

    /* Source */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-jsonp";

    /* Constants */
    public static final String CREATE_POINTER = "createPointer";
    public static final String JSON_FQ_NAME = "jakarta.json.Json";
    public static final String DIAGNOSTIC_CODE_CREATE_POINTER = "InvalidCreatePointerArg";
    public static final String JAKARTA_JSON_OBJECT_BUILDER_FQ_NAME = "jakarta.json.JsonObjectBuilder";
    public static final String JAKARTA_JSON_BUILDER_ADD_METHOD = "add";
    public static final String JAKARTA_JSON_ARRAY_BUILDER_FQ_NAME = "jakarta.json.JsonArrayBuilder";

    /* Manual JSON parsing to POJO (recommend JSON-B) */
    public static final String JSON_READER_READ_OBJECT = "readObject";
    public static final String JAKARTA_JSON_READER_FQ_NAME = "jakarta.json.JsonReader";
    public static final String JAKARTA_JSON_OBJECT_FQ_NAME = "jakarta.json.JsonObject";
    public static final String JAKARTA_JSON_PACKAGE_PREFIX = "jakarta.json.";
    public static final String SETTER_PREFIX = "set";
    public static final String GETTER_PREFIX = "get";
}
