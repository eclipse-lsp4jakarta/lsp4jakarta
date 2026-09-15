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
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.RemoveAnnotationBodyDeclarationsProposal;

/**
 * Abstract base class for quick fixes that remove body declarations (methods and fields)
 * from annotation types. Subclasses should provide the specific label and code action ID.
 *
 * This class can be extended by any module that needs to remove body declarations from
 * annotation types, making it reusable across different Jakarta EE specifications.
 */
public abstract class RemoveAnnotationBodyDeclarationsQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(RemoveAnnotationBodyDeclarationsQuickFix.class.getName());

    /**
     * Returns the label for the code action.
     *
     * @return the code action label
     */
    protected abstract String getLabel();

    /**
     * Returns the code action ID for this quick fix.
     *
     * @return the code action ID
     */
    protected abstract ICodeActionId getCodeActionId();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return this.getClass().getName();
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
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

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
            List<BodyDeclaration> bodyDeclarationsToRemove = new ArrayList<>();
            // Add all body declarations (annotation members and fields)
            List<?> bodyDeclarations = annotationTypeDecl.bodyDeclarations();
            for (Object obj : bodyDeclarations) {
                if (obj instanceof AnnotationTypeMemberDeclaration || obj instanceof FieldDeclaration) {
                    bodyDeclarationsToRemove.add((BodyDeclaration) obj);
                }
            }

            LOGGER.info("Found " + bodyDeclarationsToRemove.size() + " body declarations to remove");

            if (!bodyDeclarationsToRemove.isEmpty()) {
                ChangeCorrectionProposal proposal = new RemoveAnnotationBodyDeclarationsProposal(getLabel(), context.getCompilationUnit(), context.getASTRoot(), annotationTypeDecl, 0, bodyDeclarationsToRemove);

                try {
                    toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
                } catch (CoreException e) {
                    LOGGER.log(Level.SEVERE, "Unable to create workspace edit to remove body declarations", e);
                }
            }
        }

        return toResolve;
    }
}
