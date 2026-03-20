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
    public static final String JAKARTA_JSON_OBJECT_BUILDER_ADD = "jakarta.json.JsonObjectBuilder.add";
}
