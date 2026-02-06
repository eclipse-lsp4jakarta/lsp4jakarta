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

package org.eclipse.lsp4jakarta.jdt.internal.annotations;

import java.util.List;

import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationAttributesQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Removes the @Resource annotation from the declaring element.
 */
public class RemoveResourceAnnotationAttributeQuickFix extends RemoveAnnotationAttributesQuickFix {

    /**
     * Constructor.
     */
    public RemoveResourceAnnotationAttributeQuickFix() {
        super("jakarta.annotation.Resource", List.of("type"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveResourceAnnotationAttributeQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.RemoveResourceAnnotationAttribute;
    }

    protected String getLabel() {
        return Messages.getMessage("RemoveAttribute", "redundant 'type'", "@Resource");
    }
}
