/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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
package org.eclipse.lsp4jakarta.ls.commons.client;

/**
 * Extended client capabilities not defined by the LSP.
 *
 * Based on: https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/ls/commons/client/ExtendedClientCapabilities.java
 *
 * @author Angelo ZERR
 */
public class ExtendedClientCapabilities {

    private boolean shouldLanguageServerExitOnShutdown;

    private boolean jakartaVersionSelector;

    /**
     * Sets the boolean permitting language server to exit on client
     * shutdown() request, without waiting for client to call exit()
     *
     * @param shouldLanguageServerExitOnShutdown
     */
    public void setShouldLanguageServerExitOnShutdown(boolean shouldLanguageServerExitOnShutdown) {
        this.shouldLanguageServerExitOnShutdown = shouldLanguageServerExitOnShutdown;
    }

    /**
     * Returns true if the client should exit on shutdown() request and
     * avoid waiting for an exit() request
     *
     * @return true if the language server should exit on shutdown() request
     */
    public boolean shouldLanguageServerExitOnShutdown() {
        return shouldLanguageServerExitOnShutdown;
    }

    /**
     * Sets whether the client supports Jakarta EE version selection UI.
     *
     * @param jakartaVersionSelector true if the client supports version selection
     */
    public void setJakartaVersionSelector(boolean jakartaVersionSelector) {
        this.jakartaVersionSelector = jakartaVersionSelector;
    }

    /**
     * Returns true if the client supports Jakarta EE version selection UI.
     *
     * @return true if the client supports version selection
     */
    public boolean isJakartaVersionSelector() {
        return jakartaVersionSelector;
    }
}