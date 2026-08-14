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
package org.eclipse.lsp4jakarta.jdt.internal.ejb;

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ChangeReturnTypeToVoidQuickFix;

/**
 * Changes the return type of a session synchronization method annotated with
 * {@code @AfterBegin}, {@code @BeforeCompletion}, or {@code @AfterCompletion}
 * to {@code void}, as required by the EJB specification.
 */
public class ChangeReturnTypeToVoidForSessionSyncMethodQuickFix extends ChangeReturnTypeToVoidQuickFix {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return ChangeReturnTypeToVoidForSessionSyncMethodQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.EJBChangeReturnTypeToVoidForSessionSyncMethod;
    }
}
