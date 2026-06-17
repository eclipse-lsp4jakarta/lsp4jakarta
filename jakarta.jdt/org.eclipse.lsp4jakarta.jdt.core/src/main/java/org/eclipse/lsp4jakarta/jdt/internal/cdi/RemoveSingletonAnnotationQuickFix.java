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

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

/**
 * Removes the @Singleton annotation from a class.
 */
public class RemoveSingletonAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveSingletonAnnotationQuickFix() {
        super(false, Constants.SINGLETON_FQ_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveSingletonAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveSingletonAnnotation;
    }
}
