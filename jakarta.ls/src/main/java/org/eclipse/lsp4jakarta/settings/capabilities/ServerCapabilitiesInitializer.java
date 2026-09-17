/*******************************************************************************
* Copyright (c) 2018 Red Hat Inc. and others.
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
package org.eclipse.lsp4jakarta.settings.capabilities;

import static org.eclipse.lsp4jakarta.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_CODEACTION_OPTIONS;
import static org.eclipse.lsp4jakarta.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_COMPLETION_OPTIONS;
import static org.eclipse.lsp4jakarta.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_EXECUTE_COMMAND_OPTIONS;

import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;

/**
 * All default capabilities of this server
 *
 * Based on:
 * https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/settings/capabilities/ServerCapabilitiesInitializer.java
 */
public class ServerCapabilitiesInitializer {

    private ServerCapabilitiesInitializer() {}

    /**
     * Returns all server capabilities (with default values) that aren't dynamic.
     *
     * A service's dynamic capability is indicated by the client.
     *
     * @param clientCapabilities
     * @return ServerCapabilities object
     */
    public static ServerCapabilities getNonDynamicServerCapabilities(ClientCapabilitiesWrapper clientCapabilities) {
        ServerCapabilities serverCapabilities = new ServerCapabilities();
        serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
        if (!clientCapabilities.isCompletionDynamicRegistrationSupported()) {
            serverCapabilities.setCompletionProvider(DEFAULT_COMPLETION_OPTIONS);
        }
        if (!clientCapabilities.isCodeActionDynamicRegistered()) {
            serverCapabilities.setCodeActionProvider(DEFAULT_CODEACTION_OPTIONS);
        }
        // Register execute command capability
        serverCapabilities.setExecuteCommandProvider(DEFAULT_EXECUTE_COMMAND_OPTIONS);

        return serverCapabilities;
    }
}