/*******************************************************************************
* Copyright (c) 2025 IBM Corporation and others.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeActionKind;

/**
 * Code action proposal for removing exceptions of a method.
 * ModifyExceptionsInThrowsProposal
 */
public class ModifyExceptionsInThrowsProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit invocationNode;
    private final IBinding binding;

    // exceptions to remove
    private final List<Type> exceptionsToRemove;

    // exceptions to insert
    private final List<Type> exceptionsToAdd;

    public ModifyExceptionsInThrowsProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                            IBinding binding, int relevance, List<Type> exceptionsToRemove, List<Type> exceptionsToAdd) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.binding = binding;
        this.exceptionsToRemove = exceptionsToRemove;
        this.exceptionsToAdd = exceptionsToAdd;
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
            ListRewrite exceptionsList = rewrite.getListRewrite(declNode, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);

            // remove the exceptions
            if (null != exceptionsToRemove) {
                for (Type exception : exceptionsToRemove) {
                    exceptionsList.remove(exception, null);
                }
            }

            // insert the exceptions
            if (null != exceptionsToAdd) {
                for (Type exception : exceptionsToAdd) {
                    exceptionsList.insertLast(exception, null);
                }
            }

        }

        return rewrite;
    }
}
