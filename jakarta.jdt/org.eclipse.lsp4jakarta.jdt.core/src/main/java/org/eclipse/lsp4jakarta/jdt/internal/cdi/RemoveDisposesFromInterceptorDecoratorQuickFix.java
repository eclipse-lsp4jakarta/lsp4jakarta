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
 * Removes the @Disposes annotation from parameters in interceptor or decorator methods.
 */
public class RemoveDisposesFromInterceptorDecoratorQuickFix extends RemoveMethodParamAnnotationQuickFix {

    /**
     * Constructor.
     */
    public RemoveDisposesFromInterceptorDecoratorQuickFix() {
        super(Constants.DISPOSES_FQ_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveDisposesFromInterceptorDecoratorQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveDisposesFromInterceptorDecorator;
    }
}