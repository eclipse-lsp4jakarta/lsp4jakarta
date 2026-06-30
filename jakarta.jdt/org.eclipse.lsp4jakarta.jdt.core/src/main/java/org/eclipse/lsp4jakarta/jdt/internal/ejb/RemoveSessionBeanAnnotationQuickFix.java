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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

/**
 * Removes the session bean annotation (@Stateless, @Stateful, or @Singleton)
 * from a class that is also annotated with @Interceptor or @Decorator.
 */
public class RemoveSessionBeanAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveSessionBeanAnnotationQuickFix() {
        super(Constants.STATELESS_FQ_NAME, Constants.STATEFUL_FQ_NAME, Constants.SINGLETON_FQ_NAME);
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createCodeActions(Diagnostic diagnostic, JavaCodeActionContext context, IBinding parentType,
                                     List<CodeAction> codeActions) throws CoreException {
        // Only create code actions for the session bean annotations that are actually present
        if (parentType instanceof ITypeBinding) {
            ITypeBinding typeBinding = (ITypeBinding) parentType;
            List<String> presentAnnotations = new ArrayList<>();

            for (String annotation : getAnnotations()) {
                if (hasAnnotation(typeBinding, annotation)) {
                    presentAnnotations.add(annotation);
                }
            }

            // Create a code action for each present session bean annotation
            for (String annotation : presentAnnotations) {
                createCodeAction(diagnostic, context, parentType, codeActions, annotation);
            }
        }
    }

    /**
     * Checks if the type has the specified annotation.
     *
     * @param typeBinding The type binding
     * @param annotationFQN The fully qualified annotation name
     * @return true if the annotation is present, false otherwise
     */
    private boolean hasAnnotation(ITypeBinding typeBinding, String annotationFQN) {
        for (org.eclipse.jdt.core.dom.IAnnotationBinding annotation : typeBinding.getAnnotations()) {
            if (annotation.getAnnotationType().getQualifiedName().equals(annotationFQN)) {
                return true;
            }
        }
        return false;
    }
}

// Made with Bob
