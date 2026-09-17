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

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;

/**
 * Server Capabilities Constants
 *
 * Based on:
 * https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/settings/capabilities/ServerCapabilitiesConstants.java
 */
public class ServerCapabilitiesConstants {

    private ServerCapabilitiesConstants() {}

    public static final String TEXT_DOCUMENT_COMPLETION = "textDocument/completion";
    public static final String TEXT_DOCUMENT_CODE_ACTION = "textDocument/codeAction";

    public static final String COMPLETION_ID = UUID.randomUUID().toString();
    public static final String CODE_ACTION_ID = UUID.randomUUID().toString();

    public static final CompletionOptions DEFAULT_COMPLETION_OPTIONS = new CompletionOptions(false, null);

    public static final CodeActionOptions DEFAULT_CODEACTION_OPTIONS = createDefaultCodeActionOptions();

    public static final ExecuteCommandOptions DEFAULT_EXECUTE_COMMAND_OPTIONS = createDefaultExecuteCommandOptions();

    private static CodeActionOptions createDefaultCodeActionOptions() {
        CodeActionOptions options = new CodeActionOptions();
        options.setResolveProvider(Boolean.TRUE);
        return options;
    }

    private static ExecuteCommandOptions createDefaultExecuteCommandOptions() {
        ExecuteCommandOptions options = new ExecuteCommandOptions();
        options.setCommands(Arrays.asList("jakarta.resetVersion"));
        return options;
    }

}