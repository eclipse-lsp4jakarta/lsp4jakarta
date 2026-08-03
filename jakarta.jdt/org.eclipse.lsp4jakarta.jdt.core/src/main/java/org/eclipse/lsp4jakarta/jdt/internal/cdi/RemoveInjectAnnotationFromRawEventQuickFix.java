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

import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

/**
 * Removes the {@code @Inject} annotation from an injection point that uses a raw
 * {@code Event} type (i.e. {@code Event} without a type parameter).
 *
 * <p>This quickfix responds to the
 * {@link ErrorCode#InvalidRawEventTypeInjectionPoint} diagnostic produced by
 * {@link CdiRawEventTypeDiagnosticsParticipant}.
 */
public class RemoveInjectAnnotationFromRawEventQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveInjectAnnotationFromRawEventQuickFix() {
        super(false, "jakarta.inject.Inject");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveInjectAnnotationFromRawEventQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveInjectAnnotationFromRawEvent;
    }
}
