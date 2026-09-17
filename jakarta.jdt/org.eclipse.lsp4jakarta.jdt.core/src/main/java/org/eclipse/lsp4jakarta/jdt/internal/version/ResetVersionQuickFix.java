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
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTJakartaUtils;

/**
 * Quick fix for resetting the Jakarta EE version.
 *
 * This quick fix creates a command-based code action that triggers
 * the version reset flow on the language server side.
 */
public class ResetVersionQuickFix implements IJavaCodeActionParticipant {

    @Override
    public String getParticipantId() {
        return ResetVersionQuickFix.class.getName();
    }

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();

        // Get the project URI (not the file URI) from the context
        String projectUri = JDTJakartaUtils.getProjectURI(context.getJavaProject());

        // Create a command to reset the version
        Command command = new Command();
        command.setTitle("Reset Jakarta EE version");
        command.setCommand("jakarta.resetVersion");
        command.setArguments(Arrays.asList(projectUri));

        // Create the code action
        CodeAction codeAction = new CodeAction();
        codeAction.setTitle("Change Jakarta EE version");
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        codeAction.setCommand(command);

        codeActions.add(codeAction);

        return codeActions;
    }

    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        // No resolution needed for command-based code actions
        return context.getUnresolved();
    }

}

// Made with Bob
