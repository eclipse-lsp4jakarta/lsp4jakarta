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
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ReplaceAnnotationsQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Quickfix for InvalidInterceptorOrDecorator diagnostic.
 * Replaces all invalid scope annotations with @Dependent.
 */
public class ReplaceInvalidScopesWithDependentQuickFix extends ReplaceAnnotationsQuickFix {

    /**
     * Constructor.
     */
    public ReplaceInvalidScopesWithDependentQuickFix() {
        super(Constants.DEPENDENT_FQ_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return ReplaceInvalidScopesWithDependentQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIReplaceInvalidScopesWithDependent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCodeActionLabel(String formattedNames) {
        return Messages.getMessage("ReplaceAnnotationWith", formattedNames, "@Dependent");
    }
}
