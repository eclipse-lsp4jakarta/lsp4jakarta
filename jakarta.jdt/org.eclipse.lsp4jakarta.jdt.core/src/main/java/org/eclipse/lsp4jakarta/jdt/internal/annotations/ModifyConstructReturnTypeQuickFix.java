/*******************************************************************************
 * Copyright (c) 2023, 2026 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.annotations;

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ChangeReturnTypeToVoidQuickFix;

/**
 * Modifies the return type of the active element to {@code void}.
 */
public class ModifyConstructReturnTypeQuickFix extends ChangeReturnTypeToVoidQuickFix {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return ModifyConstructReturnTypeQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.ChangeReturnTypeToVoid;
    }
}
