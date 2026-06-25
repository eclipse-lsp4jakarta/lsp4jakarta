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
 * Removes the abstract modifier from the declaring element.
 */
public class RemoveAbstractModifierQuickFix extends RemoveModifierConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveAbstractModifierQuickFix() {
        super(false, "abstract");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveAbstractModifierQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.RemoveAbstractModifier;
    }
}
