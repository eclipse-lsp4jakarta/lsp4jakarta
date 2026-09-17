/*******************************************************************************
* Copyright (c) 2020 IBM Corporation and others.
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
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.google.gson.JsonPrimitive;

public class JakartaWorkspaceService implements WorkspaceService {

    private static final String RESET_VERSION_COMMAND = "jakarta.resetVersion";

    private final JakartaLanguageServer jakartaLanguageServer;

    public JakartaWorkspaceService(JakartaLanguageServer jls) {
        this.jakartaLanguageServer = jls;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        jakartaLanguageServer.updateSettings(params.getSettings());
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // Do nothing
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        if (RESET_VERSION_COMMAND.equals(params.getCommand())) {
            List<Object> arguments = params.getArguments();
            if (arguments != null && !arguments.isEmpty()) {
                Object firstArg = arguments.get(0);
                String projectUri = null;

                if (firstArg instanceof String) {
                    projectUri = (String) firstArg;
                } else if (firstArg instanceof JsonPrimitive) {
                    projectUri = ((JsonPrimitive) firstArg).getAsString();
                }

                if (projectUri != null) {
                    jakartaLanguageServer.resetJakartaVersion(projectUri);
                }
            }
        }
        return CompletableFuture.completedFuture(null);
    }

}
