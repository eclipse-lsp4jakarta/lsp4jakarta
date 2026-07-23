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

package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeActionKind;

/**
 * Code action proposal for removing a method from a class.
 */
public class RemoveMethodProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit invocationNode;
    private final IBinding binding;

    /**
     * Constructor for RemoveMethodProposal.
     *
     * @param label the label for the code action
     * @param targetCU the compilation unit
     * @param invocationNode the compilation unit node
     * @param binding the method binding
     * @param relevance the relevance of the proposal
     */
    public RemoveMethodProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                IBinding binding, int relevance) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.binding = binding;
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        ASTNode declNode = null;
        ASTNode boundNode = invocationNode.findDeclaringNode(binding);

        if (boundNode != null) {
            declNode = boundNode;
        } else {
            CompilationUnit newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
            declNode = newRoot.findDeclaringNode(binding.getKey());
        }

        AST ast = declNode.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        if (declNode instanceof MethodDeclaration) {
            MethodDeclaration methodDecl = (MethodDeclaration) declNode;
            ASTNode parent = methodDecl.getParent();

            if (parent instanceof TypeDeclaration) {
                ListRewrite listRewrite = rewrite.getListRewrite(parent, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
                listRewrite.remove(methodDecl, null);
            }
        }

        return rewrite;
    }
}
