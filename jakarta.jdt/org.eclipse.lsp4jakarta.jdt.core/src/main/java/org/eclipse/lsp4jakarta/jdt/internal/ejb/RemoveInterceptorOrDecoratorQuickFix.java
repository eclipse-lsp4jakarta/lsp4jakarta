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
package org.eclipse.lsp4jakarta.jdt.internal.ejb;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;

/**
 * Removes the @Interceptor or @Decorator annotation from a session bean class.
 */
public class RemoveInterceptorOrDecoratorQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveInterceptorOrDecoratorQuickFix() {
        super(Constants.INTERCEPTOR_FQ_NAME, Constants.DECORATOR_FQ_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveInterceptorOrDecoratorQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.EJBRemoveInterceptorOrDecorator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createCodeActions(Diagnostic diagnostic, JavaCodeActionContext context, IBinding parentType,
                                     List<CodeAction> codeActions) throws CoreException {
        // Only create code actions for the @Interceptor or @Decorator annotations that are actually present
        if (parentType instanceof ITypeBinding) {
            ITypeBinding typeBinding = (ITypeBinding) parentType;
            List<String> presentAnnotations = new ArrayList<>();

            for (String annotation : getAnnotations()) {
                if (DiagnosticUtils.hasAnnotation(typeBinding, annotation)) {
                    presentAnnotations.add(annotation);
                }
            }

            // Create a code action for each present annotation
            for (String annotation : presentAnnotations) {
                createCodeAction(diagnostic, context, parentType, codeActions, annotation);
            }
        }
    }
}