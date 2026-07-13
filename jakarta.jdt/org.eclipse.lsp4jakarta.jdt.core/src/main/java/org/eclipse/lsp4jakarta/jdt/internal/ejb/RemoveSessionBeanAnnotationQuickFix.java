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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

/**
 * Removes the session bean annotation (@Stateless, @Stateful, or @Singleton)
 * from a class that violates Jakarta Enterprise Beans 4.0 spec section 4.1
 * class constraints (not public, final, abstract, or not top-level).
 *
 * Only creates a code action for the session bean annotation that is actually
 * present on the class (at most one of the three).
 */
public class RemoveSessionBeanAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveSessionBeanAnnotationQuickFix() {
        super(false, Constants.STATELESS_FQ_NAME, Constants.STATEFUL_FQ_NAME, Constants.SINGLETON_FQ_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * Overrides the default to only emit a code action for the session bean
     * annotation that is actually present on the class.
     */
    @Override
    protected void createCodeActions(Diagnostic diagnostic, JavaCodeActionContext context, IBinding parentType,
                                     List<CodeAction> codeActions) throws CoreException {
        if (!(parentType instanceof ITypeBinding)) {
            return;
        }
        ITypeBinding typeBinding = (ITypeBinding) parentType;
        for (IAnnotationBinding annotationBinding : typeBinding.getAnnotations()) {
            String fqn = annotationBinding.getAnnotationType().getQualifiedName();
            boolean isSessionBeanAnnotation = Arrays.asList(getAnnotations()).contains(fqn);
            if (isSessionBeanAnnotation) {
                createCodeAction(diagnostic, context, parentType, codeActions, fqn);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveSessionBeanAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.EJBRemoveSessionBeanAnnotation;
    }
}
