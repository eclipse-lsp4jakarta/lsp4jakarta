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
package org.eclipse.lsp4jakarta.jdt.internal.ejb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import static org.eclipse.lsp4jakarta.jdt.core.ejb.EjbConstants.MESSAGE_LISTENER;
import static org.eclipse.lsp4jakarta.jdt.core.ejb.EjbConstants.MESSAGE_LISTENER_FQ_NAME;

import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ImplementInterfaceProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Quick fix for @MessageDriven beans that don't implement MessageListener interface.
 * Adds the implements clause for jakarta.jms.MessageListener to the class.
 */
public class EjbMessageDrivenImplementInterfaceQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(EjbMessageDrivenImplementInterfaceQuickFix.class.getName());

    @Override
    public String getParticipantId() {
        return EjbMessageDrivenImplementInterfaceQuickFix.class.getName();
    }

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveredNode();
        ITypeBinding parentType = Bindings.getBindingOfParentType(node);
        List<CodeAction> codeActions = new ArrayList<>();

        if (parentType != null) {
            ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel(MESSAGE_LISTENER, parentType.getName()));
            codeAction.setRelevance(0);
            codeAction.setKind(CodeActionKind.QuickFix);
            codeAction.setDiagnostics(Arrays.asList(diagnostic));
            codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), JakartaCodeActionId.EJBMessageDrivenImplementation));
            codeActions.add(codeAction);
        }

        return codeActions;
    }

    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode node = context.getCoveredNode();
        ITypeBinding parentType = Bindings.getBindingOfParentType(node);

        if (parentType == null) {
            return toResolve;
        }

        String label = getLabel(MESSAGE_LISTENER, parentType.getName());

        ChangeCorrectionProposal proposal = new ImplementInterfaceProposal(label, context.getCompilationUnit(), parentType, context.getASTRoot(), MESSAGE_LISTENER_FQ_NAME, 0);

        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action edit to implement MessageListener.", e);
        }

        return toResolve;
    }

    /**
     * Returns the code action label.
     *
     * @param interfaceName The interface name.
     * @param className The class name.
     *
     * @return The code action label.
     */
    @SuppressWarnings("restriction")
    private String getLabel(String interfaceName, String className) {
        return Messages.getMessage("LetClassImplement",
                                   org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels.getJavaElementName(className),
                                   org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels.getJavaElementName(interfaceName));
    }
}