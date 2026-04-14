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
package org.eclipse.lsp4jakarta.jdt.internal.di;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.RemoveAnnotationAttributesProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;

/**
 * Removes all attributes (methods and fields) from a @Scope annotated type.
 */
public class RemoveScopeAttributesQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(RemoveScopeAttributesQuickFix.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveScopeAttributesQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel());
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), JakartaCodeActionId.DIRemoveScopeAttributes));

        List<CodeAction> codeActions = new ArrayList<>();
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
        AnnotationTypeDeclaration annotationTypeDecl = null;
        if (node.getParent() instanceof AnnotationTypeDeclaration) {
            annotationTypeDecl = (AnnotationTypeDeclaration) node.getParent();
        }
        if (annotationTypeDecl != null) {
            // Collect all methods and fields to remove
            List<BodyDeclaration> attributesToRemove = new ArrayList<>();
            // Add all body declarations (annotation members and fields)
            List<?> bodyDeclarations = annotationTypeDecl.bodyDeclarations();
            for (Object obj : bodyDeclarations) {
                if (obj instanceof AnnotationTypeMemberDeclaration || obj instanceof FieldDeclaration) {
                    attributesToRemove.add((BodyDeclaration) obj);
                }
            }
            LOGGER.info("Found " + attributesToRemove.size() + " attributes to remove");
            if (!attributesToRemove.isEmpty()) {
                ChangeCorrectionProposal proposal = new RemoveAnnotationAttributesProposal(getLabel(), context.getCompilationUnit(), context.getASTRoot(), annotationTypeDecl, 0, attributesToRemove);
                try {
                    toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
                } catch (CoreException e) {
                    LOGGER.log(Level.SEVERE, "Unable to create workspace edit to remove scope attributes", e);
                }
            }
        }
        return toResolve;
    }

    /**
     * Returns the code action label.
     *
     * @return
     */
    public String getLabel() {
        return Messages.getMessage("RemoveScopeAttributes");
    }
}