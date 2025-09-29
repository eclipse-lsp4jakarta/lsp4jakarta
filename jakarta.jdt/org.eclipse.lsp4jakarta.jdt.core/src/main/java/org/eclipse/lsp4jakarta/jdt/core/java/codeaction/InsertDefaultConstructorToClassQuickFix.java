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
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.AddConstructorProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Inserts default constructor to the active class.
 */
public abstract class InsertDefaultConstructorToClassQuickFix implements IJavaCodeActionParticipant {
    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(InsertDefaultConstructorToClassQuickFix.class.getName());

    /**
     * Access modifier for the new constructor.
     */
    private final String accessModifier;

    /**
     * Constructor.
     *
     * @param accessModifier The access modifier to use.
     */
    public InsertDefaultConstructorToClassQuickFix(String accessModifier) {
        this.accessModifier = accessModifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertDefaultConstructorToClassQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);
        List<CodeAction> codeActions = new ArrayList<>();
        if (parentType != null) {
            ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel(accessModifier));
            codeAction.setRelevance(0);
            codeAction.setKind(CodeActionKind.QuickFix);
            codeAction.setDiagnostics(Arrays.asList(diagnostic));
            codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));
            codeActions.add(codeAction);
        }

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
        String label = getLabel(accessModifier);

        ChangeCorrectionProposal proposal = new AddConstructorProposal(label, context.getCompilationUnit(), context.getASTRoot(), parentType, 0, accessModifier);
        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action to insert a default constructor to class",
                       e);
        }

        return toResolve;
    }

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();

    /**
     * Returns the code action label.
     *
     * @param am The access modifier name.
     *
     * @return The code action label.
     */
    protected String getLabel(String am) {
        return Messages.getMessage("InsertDefaultConstructorToClass", am);
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
        if (node.getParent() instanceof VariableDeclarationFragment) {
            return ((VariableDeclarationFragment) node.getParent()).resolveBinding();
        }

        return org.eclipse.jdt.internal.corext.dom.Bindings.getBindingOfParentType(node);
    }
}
