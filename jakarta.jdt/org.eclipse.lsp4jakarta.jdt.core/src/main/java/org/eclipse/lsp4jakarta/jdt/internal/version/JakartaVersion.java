/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.version;

public enum JakartaVersion {

    EE_11(11, "Jakarta EE 11"),
    EE_10(10, "Jakarta EE 10"),
    EE_9(9, "Jakarta EE 9 / 9.1"),
    UNKNOWN(0, "Unknown / Pre-Jakarta EE 9");

    private final int level;
    private final String label;

    JakartaVersion(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
    
    public static JakartaVersion fromLevel(int version) {
        for (JakartaVersion v : JakartaVersion.values()) {
            if (v.getLevel() == version) {
                return v;
            }
        }
        return UNKNOWN;
    }

}