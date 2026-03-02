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
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ModifyAnnotationAttributeValueQuickFix;

/**
 * Quick fix for changing @Temporal annotation value to TemporalType.DATE
 * for primary key fields/properties with java.util.Date type.
 */
public class ChangeTemporalValueQuickFix extends ModifyAnnotationAttributeValueQuickFix {

    /**
     * Constructor.
     */
    public ChangeTemporalValueQuickFix() {
        super(Constants.TEMPORAL, "value", Constants.TEMPORAL_TYPE_DATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return ChangeTemporalValueQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceChangeTemporalValue;
    }
}

// Made with Bob
