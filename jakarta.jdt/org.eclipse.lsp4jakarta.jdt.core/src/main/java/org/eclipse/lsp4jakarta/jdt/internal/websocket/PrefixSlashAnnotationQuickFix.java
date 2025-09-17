/*******************************************************************************
* Copyright (c) 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.websocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.PrefixSlashAnnotationProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 *
 * QuickFix to add a slash to the beginning of annotation value
 *
 */
public class PrefixSlashAnnotationQuickFix implements IJavaCodeActionParticipant {

    private static final Logger LOGGER = Logger.getLogger(PrefixSlashAnnotationQuickFix.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return PrefixSlashAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel());
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));

        ICodeActionId id = JakartaCodeActionId.InsertSlashAnnotationValueAttribute;
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), id));
        codeActions.add(codeAction);
        return codeActions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {

        CodeAction toResolve = context.getUnresolved();
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);

        ChangeCorrectionProposal proposal = new PrefixSlashAnnotationProposal(getLabel(), context.getCompilationUnit(), context.getASTRoot(), 0, parentType, node);

        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to create workspace edit to change a method's retrun type", e);
        }
        return toResolve;
    }

    /**
     * Returns the code action label.
     *
     * @return The code action label.
     */
    private String getLabel() {
        return Messages.getMessage("PrefixSlashToValueAttribute");
    }

    /**
     * Returns the named entity associated to the given node.
     *
     * @param node The AST Node
     *
     * @return The named entity associated to the given node.
     */
    @SuppressWarnings("restriction")
    protected IBinding getBinding(ASTNode node) {
        if (node.getParent() instanceof TypeDeclaration) {
            return ((TypeDeclaration) node.getParent()).resolveBinding();
        }
        return org.eclipse.jdt.internal.corext.dom.Bindings.getBindingOfParentType(node);
    }
}
