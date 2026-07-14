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
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

/**
 * Removes conflicting @EmbeddedId or @Id annotations from the declaring element.
 *
 * Only offers removal of annotations that are actually present on the covered node.
 */
public class RemoveJPAIdentifierConflictQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveJPAIdentifierConflictQuickFix() {
        super(false, Constants.EMBEDDEDID, Constants.ID);
    }

    /**
     * {@inheritDoc}
     *
     * Overrides the base implementation to filter the candidate annotations down
     * to only those that are actually present on the node covered by the diagnostic,
     * preventing spurious "Remove @X" actions for annotations that do not exist on
     * the member.
     */
    @Override
    protected void createCodeActions(Diagnostic diagnostic, JavaCodeActionContext context,
                                     IBinding parentType, List<CodeAction> codeActions) throws CoreException {
        ASTNode coveredNode = context.getCoveredNode();
        ASTNode declaringNode = coveredNode;
        while (declaringNode != null && !(declaringNode instanceof BodyDeclaration)) {
            declaringNode = declaringNode.getParent();
        }

        if (declaringNode instanceof BodyDeclaration) {
            // Collect annotation simple names actually present on this declaration
            List<String> presentSimpleNames = new ArrayList<>();
            for (Object modifier : ((BodyDeclaration) declaringNode).modifiers()) {
                if (modifier instanceof org.eclipse.jdt.core.dom.Annotation) {
                    org.eclipse.jdt.core.dom.Annotation ann = (org.eclipse.jdt.core.dom.Annotation) modifier;
                    presentSimpleNames.add(ann.getTypeName().getFullyQualifiedName());
                }
            }

            // Only offer removal for annotations that exist on this member
            for (String candidateFqn : getAnnotations()) {
                String simpleName = candidateFqn.substring(candidateFqn.lastIndexOf('.') + 1);
                if (presentSimpleNames.contains(simpleName) || presentSimpleNames.contains(candidateFqn)) {
                    createCodeAction(diagnostic, context, parentType, codeActions, candidateFqn);
                }
            }
        } else {
            // Fallback to default behaviour if we cannot inspect the node
            super.createCodeActions(diagnostic, context, parentType, codeActions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveJPAIdentifierConflictQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceRemoveJPAIdentifierConflict;
    }
}
