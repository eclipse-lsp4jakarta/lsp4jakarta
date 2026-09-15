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
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.lsp4j.CodeActionKind;

/**
 *
 * CodeAction proposal to add slash to the beginning of the annotation value
 *
 */
public class PrefixSlashAnnotationProposal extends ASTRewriteCorrectionProposal {

    private final String VALUE_ATTRIBUTE = "value";
    private final String FORWARD_SLASH = "/";
    private final ASTNode annotationNode;

    public PrefixSlashAnnotationProposal(String label, ICompilationUnit cu, CompilationUnit invocationNode,
                                         int relevance, IBinding binding, ASTNode annotationNode) {
        super(label, CodeActionKind.QuickFix, cu, null, relevance);
        this.annotationNode = annotationNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ASTRewrite getRewrite() {

        AST ast = annotationNode.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        if (annotationNode instanceof SingleMemberAnnotation single) {
            Expression valueExpr = single.getValue();

            if (valueExpr instanceof StringLiteral oldLiteral) {
                String oldValue = oldLiteral.getLiteralValue();

                if (!oldValue.startsWith(FORWARD_SLASH)) {
                    StringLiteral newLiteral = ast.newStringLiteral();
                    newLiteral.setLiteralValue(String.format(FORWARD_SLASH + oldValue));
                    rewrite.set(single, SingleMemberAnnotation.VALUE_PROPERTY, newLiteral, null);
                }
            }

        } else if (annotationNode instanceof NormalAnnotation normal) {

            for (Object obj : normal.values()) {
                MemberValuePair pair = (MemberValuePair) obj;

                if (VALUE_ATTRIBUTE.equals(pair.getName().getIdentifier())) {
                    Expression valueExpr = pair.getValue();

                    if (valueExpr instanceof StringLiteral oldLiteral) {
                        String oldValue = oldLiteral.getLiteralValue();

                        if (!oldValue.startsWith(FORWARD_SLASH)) {
                            StringLiteral newLiteral = ast.newStringLiteral();
                            newLiteral.setLiteralValue(String.format(FORWARD_SLASH + oldValue));
                            rewrite.set(pair, MemberValuePair.VALUE_PROPERTY, newLiteral, null);
                        }
                    }
                }
            }

        }
        return rewrite;
    }
}
