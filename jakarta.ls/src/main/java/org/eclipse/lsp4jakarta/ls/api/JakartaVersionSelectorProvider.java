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

package org.eclipse.lsp4jakarta.ls.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

/**
 * Provider for Jakarta EE version selection.
 * Allows the server to request the client to show a version selection UI.
 */
public interface JakartaVersionSelectorProvider {

    /**
     * Request the client to show a Jakarta EE version selection dropdown.
     * Returns the selected version string (e.g., "9.1", "10.0") or null if cancelled.
     *
     * @param params Map containing "projectUri" (String) and "versions" (List<String>)
     * @return CompletableFuture with the selected version string or null
     */
    @JsonRequest("jakarta/selectVersion")
    CompletableFuture<String> selectJakartaVersion(Map<String, Object> params);
}
