/*******************************************************************************
* Copyright (c) 2023, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.websocket;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertAnnotationAttributesQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Inserts the @PathParam annotation with a default value attribute to a method
 * parameter.
 */
public class InsertPathParamAnnotationQuickFix extends InsertAnnotationAttributesQuickFix {

    public InsertPathParamAnnotationQuickFix() {
        super("jakarta.websocket.server.PathParam", "value");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertPathParamAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.WBInsertPathParamAnnotationWithValueAttrib;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getLabel(String annotation, String[] attributes) {
        String[] parts = annotation.split("\\.");
        String AnnotationName = (parts.length > 1) ? parts[parts.length - 1] : annotation;
        return Messages.getMessage("InsertPathParam", AnnotationName);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("restriction")
    @Override
    protected IBinding getBinding(ASTNode node) {
        // handle annotation insertions for a single variable declaration
        if (node.getParent() instanceof SingleVariableDeclaration) {
            return ((SingleVariableDeclaration) node.getParent()).resolveBinding();
        }

        if (node.getParent() instanceof VariableDeclarationFragment) {
            return ((VariableDeclarationFragment) node.getParent()).resolveBinding();
        }
        return org.eclipse.jdt.internal.corext.dom.Bindings.getBindingOfParentType(node);
    }
}
