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
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;

/**
 * Inserts the {@code @Entity} annotation on a class that carries
 * {@code @AssociationOverride} or {@code @AssociationOverrides} but lacks the
 * required {@code @Entity}, {@code @MappedSuperclass}, or {@code @Embeddable}
 * annotation.
 *
 * @see <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13942">
 *      Jakarta Persistence 3.0, Section 13.1.1</a>
 */
public class AddEntityAnnotationQuickFix extends InsertAnnotationMissingQuickFix {

    /**
     * Constructor.
     */
    public AddEntityAnnotationQuickFix() {
        super(Constants.ENTITY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return AddEntityAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceAddEntityAnnotation;
    }
}
