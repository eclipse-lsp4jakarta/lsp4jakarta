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

import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;

/**
 * Removes the static modifier from the declaring element.
 */
public class RemoveStaticModifierQuickFix extends RemoveModifierConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveStaticModifierQuickFix() {
        super(false, "static");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveStaticModifierQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.RemoveStaticModifier;
    }
}
