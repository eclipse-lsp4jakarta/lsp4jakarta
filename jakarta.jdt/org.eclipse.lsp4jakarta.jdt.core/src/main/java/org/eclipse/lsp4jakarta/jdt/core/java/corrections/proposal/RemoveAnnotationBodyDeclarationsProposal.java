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
package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.lsp4j.CodeActionKind;

/**
 * Code action proposal for removing body declarations (methods and fields) from an annotation type.
 */
public class RemoveAnnotationBodyDeclarationsProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit invocationNode;
    private final AnnotationTypeDeclaration annotationTypeDeclaration;
    private final List<BodyDeclaration> bodyDeclarationsToRemove;

    /**
     * Constructor for RemoveAnnotationBodyDeclarationsProposal.
     *
     * @param label The label for this proposal
     * @param targetCU The compilation unit
     * @param invocationNode The compilation unit AST node
     * @param annotationTypeDeclaration The annotation type declaration to modify
     * @param relevance The relevance of this proposal
     * @param bodyDeclarationsToRemove The list of body declarations (methods/fields) to remove
     */
    public RemoveAnnotationBodyDeclarationsProposal(String label, ICompilationUnit targetCU,
                                                    CompilationUnit invocationNode,
                                                    AnnotationTypeDeclaration annotationTypeDeclaration,
                                                    int relevance,
                                                    List<BodyDeclaration> bodyDeclarationsToRemove) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.annotationTypeDeclaration = annotationTypeDeclaration;
        this.bodyDeclarationsToRemove = bodyDeclarationsToRemove;
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        AST ast = annotationTypeDeclaration.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        // Get the list rewriter for the annotation type's body declarations
        ListRewrite listRewrite = rewrite.getListRewrite(annotationTypeDeclaration, AnnotationTypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        // Remove all body declarations
        for (BodyDeclaration bodyDeclaration : bodyDeclarationsToRemove) {
            listRewrite.remove(bodyDeclaration, null);
        }

        return rewrite;
    }
}
