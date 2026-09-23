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
 * Inserts either @Entity or @MappedSuperclass on a class that has @NamedQuery
 * or @NamedNativeQuery but is missing both required annotations. Two separate
 * code actions are offered, one per annotation.
 */
public class InsertEntityOrMappedSuperclassAnnotationQuickFix extends InsertAnnotationMissingQuickFix {

    public InsertEntityOrMappedSuperclassAnnotationQuickFix() {
        super(Constants.ENTITY, Constants.MAPPEDSUPERCLASS);
    }

    @Override
    public String getParticipantId() {
        return InsertEntityOrMappedSuperclassAnnotationQuickFix.class.getName();
    }

    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceInsertEntityOrMappedSuperclassAnnotation;
    }
}
