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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4jakarta.commons.utils.AnnotationValueExpressionUtil;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;

/**
 * Code action proposal for modifying an existing annotation's attribute value.
 * Supports:
 * <ul>
 * <li>String literals: pass value wrapped in double quotes, e.g., {@code "\"abc.def\""}</li>
 * <li>Number literals: pass numeric value as string, e.g., {@code "42"}, {@code "3.14"}, {@code "100L"}</li>
 * <li>Boolean literals: pass {@code "true"} or {@code "false"}</li>
 * <li>Qualified names (enum constants): e.g., {@code "TemporalType.DATE"}</li>
 * <li>Simple names: e.g., {@code "DATE"}</li>
 * </ul>
 */
public class ModifyAnnotationAttributeValueProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit fInvocationNode;
    private final IBinding fBinding;
    private final Annotation fAnnotationNode;
    private final String annotation;
    private final String attributeName;
    private final Object newValue;

    /**
     * Constructor for modifying annotation attribute value using binding.
     */
    public ModifyAnnotationAttributeValueProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                                  IBinding binding, int relevance, String annotation,
                                                  String attributeName, Object newValue) {
        this(label, targetCU, invocationNode, binding, null, relevance, annotation, attributeName, newValue);
    }

    /**
     * Constructor for modifying annotation attribute value using annotation node directly.
     */
    public ModifyAnnotationAttributeValueProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                                  IBinding binding, Annotation annotationNode, int relevance, String annotation,
                                                  String attributeName, Object newValue) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        fInvocationNode = invocationNode;
        fBinding = binding;
        fAnnotationNode = annotationNode;
        this.annotation = annotation;
        this.attributeName = attributeName;
        this.newValue = newValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        // If we have the annotation node directly, use it
        if (fAnnotationNode != null) {
            AST ast = fAnnotationNode.getAST();
            ASTRewrite rewrite = ASTRewrite.create(ast);
            ImportRewrite imports = createImportRewrite(fInvocationNode);
            ImportRewriteContext importRewriteContext = new ContextSensitiveImportRewriteContext(fAnnotationNode, imports);

            return modifyAnnotationValue(fAnnotationNode, ast, rewrite, imports, importRewriteContext);
        }

        // Otherwise, find the declaring node using the binding
        ASTNode declNode = null;
        ASTNode boundNode = fInvocationNode.findDeclaringNode(fBinding);
        CompilationUnit newRoot = fInvocationNode;

        if (boundNode != null) {
            declNode = boundNode;
        } else {
            newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
            declNode = newRoot.findDeclaringNode(fBinding.getKey());
        }

        ImportRewrite imports = createImportRewrite(newRoot);

        boolean isField = declNode instanceof VariableDeclarationFragment;
        if (isField) {
            declNode = declNode.getParent();
        }

        AST ast = declNode.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        ImportRewriteContext importRewriteContext = new ContextSensitiveImportRewriteContext(declNode, imports);

        // Get the annotation short name for comparison
        String annotationShortName = annotation.substring(annotation.lastIndexOf('.') + 1);

        // Determine the property descriptor based on the node type
        ChildListPropertyDescriptor property;
        if (declNode instanceof TypeDeclaration) {
            property = TypeDeclaration.MODIFIERS2_PROPERTY;
        } else if (declNode instanceof FieldDeclaration) {
            property = FieldDeclaration.MODIFIERS2_PROPERTY;
        } else if (declNode instanceof MethodDeclaration) {
            property = MethodDeclaration.MODIFIERS2_PROPERTY;
        } else if (declNode instanceof SingleVariableDeclaration) {
            property = SingleVariableDeclaration.MODIFIERS2_PROPERTY;
        } else {
            return null;
        }

        // Find the annotation to modify
        List<? extends ASTNode> modifiers = (List<? extends ASTNode>) declNode.getStructuralProperty(property);
        for (ASTNode modifier : modifiers) {
            Annotation foundAnnotation = null;
            String typeName = null;

            if (modifier instanceof NormalAnnotation) {
                foundAnnotation = (NormalAnnotation) modifier;
                typeName = ((NormalAnnotation) modifier).getTypeName().toString();
            } else if (modifier instanceof SingleMemberAnnotation) {
                foundAnnotation = (SingleMemberAnnotation) modifier;
                typeName = ((SingleMemberAnnotation) modifier).getTypeName().toString();
            }

            if (foundAnnotation != null && (typeName.equals(annotationShortName) || typeName.equals(annotation))) {
                return modifyAnnotationValue(foundAnnotation, ast, rewrite, imports, importRewriteContext);
            }
        }

        return rewrite;
    }

    /**
     * Helper method to modify an annotation's attribute value.
     * Handles both NormalAnnotation and SingleMemberAnnotation.
     */
    private ASTRewrite modifyAnnotationValue(Annotation targetAnnotation, AST ast, ASTRewrite rewrite,
                                             ImportRewrite imports, ImportRewrite.ImportRewriteContext importRewriteContext) {
        // Create the new value expression
        Expression newValueExpression = AnnotationValueExpressionUtil.createValueExpression(ast, newValue, annotation, imports, importRewriteContext);

        if (targetAnnotation instanceof NormalAnnotation) {
            // Handle NormalAnnotation: @Temporal(value = TemporalType.TIME)
            NormalAnnotation normalAnnotation = (NormalAnnotation) targetAnnotation;
            List<MemberValuePair> values = normalAnnotation.values();
            for (MemberValuePair mvp : values) {
                if (mvp.getName().toString().equals(attributeName)) {
                    rewrite.set(mvp, MemberValuePair.VALUE_PROPERTY, newValueExpression, null);
                    return rewrite;
                }
            }
        } else if (targetAnnotation instanceof SingleMemberAnnotation) {
            // Handle SingleMemberAnnotation: @Temporal(TemporalType.TIME)
            // For single member annotations, the attribute is implicitly "value"
            if ("value".equals(attributeName)) {
                SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) targetAnnotation;
                rewrite.set(singleMemberAnnotation, SingleMemberAnnotation.VALUE_PROPERTY, newValueExpression, null);
                return rewrite;
            }
        }

        return rewrite;
    }
}

// Made with Bob
