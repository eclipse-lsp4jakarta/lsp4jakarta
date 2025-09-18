/*******************************************************************************
* Copyright (c) 2021, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation, Bera Sogut - initial API and implementation
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
import org.eclipse.lsp4jakarta.jdt.internal.jaxrs.RemoveMethodEntityParamsWithExclusionQuickFix;

/**
 * Code action proposal for removing parameters of a method except one. Used by
 * JAX-RS ResourceMethodMultipleEntityParamsQuickFix.
 *
 * @author Bera Sogut
 * @see CodeActionHandler
 * @see RemoveMethodEntityParamsWithExclusionQuickFix
 *
 */
public class RemoveThrownExceptionsProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit invocationNode;
    private final IBinding binding;

    // parameters to remove
    private final List<Type> exceptions;

    public RemoveThrownExceptionsProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                          IBinding binding, int relevance, List<Type> exceptions) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.binding = binding;
        this.exceptions = exceptions;
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
            for (Type exception : exceptions) {
                exceptionsList.remove(exception, null);
            }
        }

        return rewrite;
    }
}
