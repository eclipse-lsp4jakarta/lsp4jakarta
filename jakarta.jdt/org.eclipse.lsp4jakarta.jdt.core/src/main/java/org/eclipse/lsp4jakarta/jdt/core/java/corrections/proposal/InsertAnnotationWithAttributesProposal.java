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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4jakarta.commons.utils.AnnotationValueExpressionUtil;

/**
 * Code action proposal for inserting an annotation with multiple attribute values.
 * Supports:
 * <ul>
 * <li>String literals: pass value wrapped in double quotes, e.g., {@code "\"abc.def\""}</li>
 * <li>Number literals: pass numeric value as string, e.g., {@code "42"}, {@code "3.14"}, {@code "100L"}</li>
 * <li>Boolean literals: pass {@code "true"} or {@code "false"}</li>
 * <li>Qualified names (enum constants): e.g., {@code "TemporalType.DATE"}</li>
 * <li>Simple names: e.g., {@code "DATE"}</li>
 * </ul>
 * <p>
 * Example usage:
 *
 * <pre>
 * Map<String, Object> attrs = new LinkedHashMap<>();
 * attrs.put("name", "\"id\"");
 * attrs.put("nullable", "false");
 * new InsertAnnotationWithAttributesProposal(..., "jakarta.persistence.Column", attrs);
 * // Produces: @Column(name = "id", nullable = false)
 * </pre>
 */
public class InsertAnnotationWithAttributesProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit fInvocationNode;
    private final IBinding fBinding;
    private final String annotation;
    private final Map<String, Object> attributes;

    /**
     * Constructor for inserting an annotation with multiple attributes.
     *
     * @param label The label for the code action
     * @param targetCU The compilation unit to modify
     * @param invocationNode The compilation unit AST root
     * @param binding The binding of the field/method to annotate
     * @param relevance The relevance of this proposal
     * @param annotation The fully qualified annotation name (e.g., "jakarta.persistence.Column")
     * @param attributes Map of attribute names to values (order is preserved if LinkedHashMap is used)
     */
    public InsertAnnotationWithAttributesProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                                  IBinding binding, int relevance, String annotation,
                                                  Map<String, Object> attributes) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        fInvocationNode = invocationNode;
        fBinding = binding;
        this.annotation = annotation;
        this.attributes = attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ASTRewrite getRewrite() throws CoreException {
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

        // Create the annotation
        NormalAnnotation marker = ast.newNormalAnnotation();
        marker.setTypeName(ast.newName(imports.addImport(annotation, importRewriteContext)));

        // Add all attributes with their values
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            MemberValuePair mvp = ast.newMemberValuePair();
            mvp.setName(ast.newSimpleName(entry.getKey()));

            Expression valueExpression = AnnotationValueExpressionUtil.createValueExpression(
                                                                                             ast, entry.getValue(), annotation, imports, importRewriteContext);
            mvp.setValue(valueExpression);

            marker.values().add(mvp);
        }

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

        rewrite.getListRewrite(declNode, property).insertFirst(marker, null);

        return rewrite;
    }
}

// Made with Bob