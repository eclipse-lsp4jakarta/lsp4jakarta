/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.snippets;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the type of the content of a snippet.
 */
public enum SnippetContentType {

    @SerializedName("class")
    CLASS, //

    @SerializedName("before-class")
    BEFORE_CLASS, //

    @SerializedName("method")
    METHOD, //

    @SerializedName("field")
    FIELD, //

    @SerializedName("method-annotation")
    METHOD_ANNOTATION;

}
