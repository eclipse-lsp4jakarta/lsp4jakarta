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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyAnnotationProposal;

/**
 * Removes the 'notifyObserver' attribute from @Observes or @ObservesAsync annotations
 * when they are conditional observers (notifyObserver=IF_EXISTS) on @Dependent scoped beans.
 */
public class RemoveNotifyObserverAttributeQuickFix implements IJavaCodeActionParticipant {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveNotifyObserverAttributeQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        ASTNode node = context.getCoveredNode();
        MethodDeclaration parentNode = (MethodDeclaration) node.getParent();
        IMethodBinding parentMethod = parentNode.resolveBinding();

        if (parentMethod == null) {
            return codeActions;
        }

        // Get the declaring type
        ITypeBinding parentType = parentMethod.getDeclaringClass();

        // Iterate through method parameters to find @Observes/@ObservesAsync with notifyObserver attribute
        List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) parentNode.parameters();

        for (SingleVariableDeclaration parameter : parameters) {
            List<ASTNode> modifiers = (List<ASTNode>) parameter.getStructuralProperty(SingleVariableDeclaration.MODIFIERS2_PROPERTY);

            for (ASTNode modifier : modifiers) {
                if (modifier instanceof Annotation) {
                    Annotation annotation = (Annotation) modifier;
                    IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();

                    if (annotationBinding != null) {
                        String annotationFQName = annotationBinding.getAnnotationType().getQualifiedName();

                        // Check if it's @Observes or @ObservesAsync with notifyObserver attribute
                        if ((annotationFQName.equals(Constants.OBSERVES_FQ_NAME) ||
                             annotationFQName.equals(Constants.OBSERVES_ASYNC_FQ_NAME))
                            &&
                            isConditionalObserver(annotationBinding)) {

                            // Create list of attributes to remove
                            ArrayList<String> attributesToRemove = new ArrayList<>();
                            attributesToRemove.add("notifyObserver");

                            String annotationName = annotationBinding.getAnnotationType().getName();
                            String name = "Remove 'notifyObserver' attribute from @" + annotationName;

                            // Get the parameter binding
                            IBinding parameterBinding = parameter.resolveBinding();

                            // Create ModifyAnnotationProposal with the parameter binding
                            ChangeCorrectionProposal proposal = new ModifyAnnotationProposal(name, context.getCompilationUnit(), context.getASTRoot(), parameterBinding, 0, annotationFQName, new ArrayList<String>(), // Empty attributesToAdd
                                            attributesToRemove // Attributes to remove
                            );

                            CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
                            codeAction.setTitle(name);
                            if (codeAction != null) {
                                codeActions.add(codeAction);
                            }
                        }
                    }
                }
            }
        }

        return codeActions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        return null;
    }

    /**
     * Returns the code action id for this quick fix.
     *
     * @return the code action id
     */
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveNotifyObserverAttribute;
    }

    /**
     * Checks if the annotation is a conditional observer (has notifyObserver=IF_EXISTS).
     *
     * @param annotation the annotation binding to check
     * @return true if the annotation has notifyObserver=IF_EXISTS, false otherwise
     */
    private boolean isConditionalObserver(IAnnotationBinding annotation) {
        for (IMemberValuePairBinding pair : annotation.getAllMemberValuePairs()) {
            if ("notifyObserver".equals(pair.getName())) {
                Object value = pair.getValue();
                if (value != null && value.toString().contains("IF_EXISTS")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the type binding for the given node.
     *
     * @param node the AST node
     * @return the type binding or null
     */
    private static ITypeBinding getBinding(ASTNode node) {
        if (node.getParent() instanceof org.eclipse.jdt.core.dom.TypeDeclaration) {
            return ((org.eclipse.jdt.core.dom.TypeDeclaration) node.getParent()).resolveBinding();
        }
        return null;
    }
}

// Made with Bob
