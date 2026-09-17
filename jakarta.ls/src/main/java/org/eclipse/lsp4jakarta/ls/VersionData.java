/*******************************************************************************
* Copyright (c) 2020, 2023 IBM Corporation and others.
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

import java.util.List;

/**
 * Data class to represent the version information stored in JSON format.
 * Contains the selected Jakarta EE version, selection mode, and available versions.
 */
public class VersionData {
    private String version;
    private String selectionMode;
    private List<String> availableVersions;

    public VersionData() {}

    public VersionData(String version, String selectionMode, List<String> availableVersions) {
        this.version = version;
        this.selectionMode = selectionMode;
        this.availableVersions = availableVersions;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(String selectionMode) {
        this.selectionMode = selectionMode;
    }

    public List<String> getAvailableVersions() {
        return availableVersions;
    }

    public void setAvailableVersions(List<String> availableVersions) {
        this.availableVersions = availableVersions;
    }
}
