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

import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

/**
 * Removes conflicting @EmbeddedId or @Id annotations from the declaring element.
 * Applicable to:
 * - Multiple @EmbeddedId declarations on the same entity (MultipleEmbeddedIdAnnotations)
 * - Mixed @Id and @EmbeddedId usage on the same entity (MixedIdentifierAnnotations)
 */
public class RemoveNamedJPAAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveNamedJPAAnnotationQuickFix() {
        super(false, Constants.EMBEDDEDID, Constants.ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveNamedJPAAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceRemoveNamedJPAAnnotation;
    }
}
