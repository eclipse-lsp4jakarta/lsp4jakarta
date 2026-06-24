/*******************************************************************************
* Copyright (c) 2024 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertAnnotationAttributeQuickFix;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.InsertAnnotationAttributeProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Quick fix for inserting the 'value' attribute to @Named annotation
 * on non-field injection points (constructor/method parameters).
 *
 * According to CDI spec, @Named on constructor/method parameters must
 * specify a value, otherwise it's a definition error.
 */
public class InsertNamedValueAttributeQuickFix extends InsertAnnotationAttributeQuickFix {

    private static final Logger LOGGER = Logger.getLogger(InsertNamedValueAttributeQuickFix.class.getName());

    /**
     * Constructor.
     */
    public InsertNamedValueAttributeQuickFix() {
        super("value");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertNamedValueAttributeQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIInsertNamedValueAttribute;
    }

    /**
     * Override to provide custom label with annotation name.
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        String label = Messages.getMessage("InsertAttributes", "value", "", "Named");
        ExtendedCodeAction codeAction = new ExtendedCodeAction(label);
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));
        return Collections.singletonList(codeAction);
    }

    /**
     * {@inheritDoc}
     *
     * Override to handle the case where @Named is on a method/constructor parameter.
     * The AST structure is: Annotation -> SingleVariableDeclaration -> MethodDeclaration
     * We need to find the Annotation node directly.
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode selectedNode = context.getCoveringNode();

        // Find the Annotation node - it should be the selectedNode itself or a parent
        Annotation annotation = null;
        ASTNode node = selectedNode;
        while (node != null && !(node instanceof Annotation)) {
            node = node.getParent();
        }

        if (node instanceof Annotation) {
            annotation = (Annotation) node;
        }

        if (annotation != null) {
            String name = Messages.getMessage("InsertAttributes", "value", "", "Named");
            ChangeCorrectionProposal proposal = new InsertAnnotationAttributeProposal(name, context.getCompilationUnit(), annotation, 0, "value");
            try {
                toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
            } catch (CoreException e) {
                LOGGER.log(Level.SEVERE, "Unable to resolve code action edit for inserting value attribute to @Named", e);
            }
        } else {
            LOGGER.log(Level.WARNING, "Could not find Annotation node for @Named quick fix");
        }

        return toResolve;
    }
}