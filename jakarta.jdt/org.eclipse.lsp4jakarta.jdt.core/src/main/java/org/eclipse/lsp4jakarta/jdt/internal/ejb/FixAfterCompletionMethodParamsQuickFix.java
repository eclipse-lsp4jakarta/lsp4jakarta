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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.ejb;

import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ReplaceParametersWithSingleParamQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * QuickFix for {@code InvalidAfterCompletionMethodParams}: fixes the parameter
 * list of an {@code @AfterCompletion} method so it declares exactly one
 * {@code boolean} parameter, as required by the EJB specification.
 *
 * <p>Extends the generic {@link ReplaceParametersWithSingleParamQuickFix}, supplying
 * {@code boolean} as the required type and delegating all AST rewriting to
 * {@link org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ReplaceParametersWithSingleParamProposal}.
 */
public class FixAfterCompletionMethodParamsQuickFix extends ReplaceParametersWithSingleParamQuickFix {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return FixAfterCompletionMethodParamsQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PrimitiveType.Code getRequiredPrimitiveType() {
        return PrimitiveType.BOOLEAN;
    }

    /**
     * {@inheritDoc}
     * <p>Used as the inserted parameter name when the method has no parameters at all.
     */
    @Override
    protected String getDefaultParamName() {
        return "committed";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getLabel() {
        return Messages.getMessage("FixAfterCompletionMethodParam");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.EJBFixAfterCompletionMethodParams;
    }
}
