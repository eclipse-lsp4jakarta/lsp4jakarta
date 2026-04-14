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

/**
 * Quickfix for removing observer annotations (@Observes or @ObservesAsync)
 * from method parameters when multiple parameters have observer annotations.
 */
public class RemoveMultipleObserverParamsQuickFix extends RemoveMethodParamAnnotationQuickFix {

    /**
     * Constructor.
     */
    public RemoveMultipleObserverParamsQuickFix() {
        super(Constants.OBSERVES_FQ_NAME, Constants.OBSERVES_ASYNC_FQ_NAME);
    }

    @Override
    public String getParticipantId() {
        return RemoveMultipleObserverParamsQuickFix.class.getName();
    }

    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveAllObserverParamsExceptOne;
    }
}
