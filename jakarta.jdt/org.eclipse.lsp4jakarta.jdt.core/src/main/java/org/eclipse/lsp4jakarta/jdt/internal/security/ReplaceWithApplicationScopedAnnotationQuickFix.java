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
package org.eclipse.lsp4jakarta.jdt.internal.security;

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ReplaceAnnotationsQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Quickfix for {@code InvalidScopeOnIdentityStoreDefinition} diagnostic.
 *
 * <p>Replaces the invalid scope annotation(s) with {@code @ApplicationScoped} on a class
 * annotated with {@code @LdapIdentityStoreDefinition} or {@code @DatabaseIdentityStoreDefinition}.</p>
 */
public class ReplaceWithApplicationScopedAnnotationQuickFix extends ReplaceAnnotationsQuickFix {

    /**
     * Constructor.
     */
    public ReplaceWithApplicationScopedAnnotationQuickFix() {
        super(Constants.APPLICATION_SCOPED_FQ_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return ReplaceWithApplicationScopedAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.SecurityReplaceWithApplicationScopedAnnotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCodeActionLabel(String formattedNames) {
        return Messages.getMessage("ReplaceAnnotationWith", formattedNames, "@ApplicationScoped");
    }
}
