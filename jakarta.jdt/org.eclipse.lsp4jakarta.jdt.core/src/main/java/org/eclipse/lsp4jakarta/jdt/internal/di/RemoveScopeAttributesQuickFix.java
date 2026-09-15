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
package org.eclipse.lsp4jakarta.jdt.internal.di;

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationBodyDeclarationsQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Quick fix for removing all body declarations (methods and fields) from a @Scope annotated type.
 */
public class RemoveScopeAttributesQuickFix extends RemoveAnnotationBodyDeclarationsQuickFix {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getLabel() {
        return Messages.getMessage("RemoveScopeAttributes");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.DIRemoveScopeAttributes;
    }
}