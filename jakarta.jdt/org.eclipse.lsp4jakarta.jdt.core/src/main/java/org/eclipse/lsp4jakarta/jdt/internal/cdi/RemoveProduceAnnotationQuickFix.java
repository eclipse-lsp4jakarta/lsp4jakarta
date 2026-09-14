/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
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
 * Removes the @Produces annotation from the declaring element.
 */
public class RemoveProduceAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveProduceAnnotationQuickFix() {
        super(false, "jakarta.enterprise.inject.Produces");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveProduceAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveProducesAnnotation;
    }
}
