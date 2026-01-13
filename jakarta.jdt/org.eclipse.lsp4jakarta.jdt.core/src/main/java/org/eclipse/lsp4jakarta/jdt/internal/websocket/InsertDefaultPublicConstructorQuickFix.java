/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
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

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertDefaultConstructorToClassQuickFix;

/**
 * Inserts a public default constructor the active class.
 */
public class InsertDefaultPublicConstructorQuickFix extends InsertDefaultConstructorToClassQuickFix {

    /**
     * Constructor.
     */
    public InsertDefaultPublicConstructorQuickFix() {
        super("public");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertDefaultPublicConstructorQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceInsertPublicCtrtToClass;
    }
}
