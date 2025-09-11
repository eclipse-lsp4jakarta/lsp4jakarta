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
package org.eclipse.lsp4jakarta.jdt.internal.di;

import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertModifierToNestedClassQuickFix;

/**
 * Insert the static modifier.
 */
public class InsertStaticModifierQuickFix extends InsertModifierToNestedClassQuickFix {

    /**
     * Constructor.
     */
    public InsertStaticModifierQuickFix() {
        super("static");
    }

    /**
     * {@inheritDoc}
     */
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.MakeMethodStatic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertStaticModifierQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isModifierExist(TypeDeclaration typeDeclaration) {
        if (Modifier.isStatic(typeDeclaration.getModifiers())) {
            return true;
        }
        return false;
    }

}
