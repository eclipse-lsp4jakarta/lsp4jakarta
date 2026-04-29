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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Removes the @Dependent annotation from the declaring type.
 */
public class RemoveDependentScopeQuickFix extends RemoveAnnotationConflictQuickFix {

    public RemoveDependentScopeQuickFix() {
        super(false, Constants.DEPENDENT_FQ_NAME);
    }

    @Override
    public String getParticipantId() {
        return RemoveDependentScopeQuickFix.class.getName();
    }

    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveDependentScope;
    }

    @Override
    protected IBinding getBinding(ASTNode node) {
        IBinding binding = super.getBinding(node);
        return (binding instanceof IMethodBinding) ? ((IMethodBinding) binding).getDeclaringClass() : binding;
    }

    @Override
    protected ASTNode getDeclaringNode(JavaCodeActionResolveContext context) {
        ASTNode node = context.getCoveredNode().getParent();
        while (node != null && !(node instanceof TypeDeclaration)) {
            node = node.getParent();
        }
        return node != null ? node : super.getDeclaringNode(context);
    }

    @Override
    protected String getLabel(String[] annotations) {
        return Messages.getMessage("RemoveDependentScope");
    }
}
// Made with Bob
