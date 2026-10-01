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

package org.eclipse.lsp4jakarta.ls;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the mode by which a Jakarta EE version was resolved for a project.
 */
public enum SelectionMode {

    @SerializedName("singleVersion")
    SINGLE_VERSION("singleVersion"),

    @SerializedName("selected")
    USER_SELECTED("selected"),

    @SerializedName("default")
    DEFAULT("default");

    private final String value;

    SelectionMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static SelectionMode fromValue(String value) {
        if (value == null) {
            return DEFAULT;
        }
        for (SelectionMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value) || mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        // Handle legacy or variant strings
        if ("defaultVersion".equalsIgnoreCase(value)) {
            return DEFAULT;
        }
        if ("userSelected".equalsIgnoreCase(value) || "user_selected".equalsIgnoreCase(value)) {
            return USER_SELECTED;
        }
        return DEFAULT;
    }
}
