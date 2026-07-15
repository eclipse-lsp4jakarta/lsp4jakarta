/*******************************************************************************
* Copyright (c) 2021, 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation, Jianing Xu - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.TextEdit;

/**
 *
 * Code action proposal for deleting an existing annotation for
 * MethodDeclaration/Field.
 *
 * Author: Jianing Xu
 *
 */
public class RemoveAnnotationProposal extends ASTRewriteCorrectionProposal {
    private final CompilationUnit fInvocationNode;
    private final IBinding fBinding;

    private final String[] annotations;
    private final ASTNode declaringNode;

    /**
     * Constructor for DeleteAnnotationProposal
     *
     * @param label - annotation label
     * @param targetCU - the entire Java compilation unit
     * @param invocationNode
     * @param binding
     * @param relevance
     * @param declaringNode - declaringNode covered node of diagnostic
     * @param annotations
     */
    public RemoveAnnotationProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                    IBinding binding, int relevance, ASTNode declaringNode, String... annotations) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.fInvocationNode = invocationNode;
        this.fBinding = binding;
        this.declaringNode = declaringNode;
        this.annotations = annotations;
    }

    @Override
    protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
        // Call CUCorrectionProposal.addEdits() (empty default), not
        // ASTRewriteCorrectionProposal.addEdits(), since we handle edits directly.
        super.addEdits(document, editRoot);

        ASTNode declNode = this.declaringNode;
        ASTNode boundNode = fInvocationNode.findDeclaringNode(fBinding);
        CompilationUnit newRoot = fInvocationNode;
        if (boundNode == null) {
            newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
        }
        ImportRewrite imports = createImportRewrite(newRoot);
        if (declNode instanceof VariableDeclarationFragment) {
            declNode = declNode.getParent();
        }
        boolean isField = declNode instanceof FieldDeclaration;
        boolean isMethod = declNode instanceof MethodDeclaration;
        boolean isType = declNode instanceof TypeDeclaration;
        boolean isParam = declNode instanceof SingleVariableDeclaration;

        if (!(isField || isMethod || isType || isParam)) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<? extends ASTNode> children;
        if (isMethod) {
            children = (List<? extends ASTNode>) declNode.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);
        } else if (isType) {
            children = (List<? extends ASTNode>) declNode.getStructuralProperty(TypeDeclaration.MODIFIERS2_PROPERTY);
        } else if (isParam) {
            children = (List<? extends ASTNode>) declNode.getStructuralProperty(SingleVariableDeclaration.MODIFIERS2_PROPERTY);
        } else {
            children = (List<? extends ASTNode>) declNode.getStructuralProperty(FieldDeclaration.MODIFIERS2_PROPERTY);
        }

        String source = document.get();

        for (ASTNode child : children) {
            if (!(child instanceof Annotation)) {
                continue;
            }
            Annotation annotation = (Annotation) child;
            // Skip over annotations that don't match those requested to delete
            String matchingFqn = Arrays.stream(getAnnotations()).filter(fqn -> matchesAnnotation(fqn, annotation.getTypeName().toString())).findFirst().orElse(null);
            if (matchingFqn == null) {
                continue;
            }
            ITypeBinding binding = annotation.resolveTypeBinding();
            if (!binding.getQualifiedName().equals(matchingFqn)) {
                continue;
            }

            // Finding line boundaries
            int annotStart = child.getStartPosition(); // where @ starts
            int annotEnd = annotStart + child.getLength(); // where annotation ends

            // Find the start of the line containing this annotation.
            int lineStart = annotStart;
            while (lineStart > 0 && source.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }

            // Find the end of the line containing this annotation.
            int lineEnd = annotEnd;
            while (lineEnd < source.length() && source.charAt(lineEnd) != '\n') {
                lineEnd++;
            }

            // Check whether the annotation is the only non-whitespace token on its line.
            String before = source.substring(lineStart, annotStart);
            String after = source.substring(annotEnd, lineEnd);
            boolean aloneOnLine = before.trim().isEmpty() && after.trim().isEmpty();

            int deleteStart;
            int deleteEnd;

            if (aloneOnLine) {
                // Delete the entire line including its leading indentation and trailing newline,
                // so no blank line is left behind.
                deleteStart = lineStart;
                deleteEnd = (lineEnd < source.length() && source.charAt(lineEnd) == '\n') ? lineEnd + 1 : lineEnd;
            } else {
                // The annotation shares its line with other tokens.
                // Every annotation eats its trailing space (the separator after it).
                // First token also eats leading indentation (deleteStart = lineStart).
                // Later tokens start at annotStart since their preceding space was already
                // consumed by the previous token's trailing-space range.
                // Ranges are disjoint.
                boolean atLineStart = before.trim().isEmpty();

                deleteStart = atLineStart ? lineStart : annotStart;
                deleteEnd = annotEnd;
                // Eat one trailing space if one exists on the same line.
                if (deleteEnd < lineEnd) {
                    char c = source.charAt(deleteEnd);
                    if (c == ' ' || c == '\t') {
                        deleteEnd++;
                    }
                }
            }

            editRoot.addChild(new DeleteEdit(deleteStart, deleteEnd - deleteStart));
        }

        // Rewrite imports if the import rewrite has accumulated changes.
        if (imports != null) {
            TextEdit importEdit = imports.rewriteImports(null);
            if (importEdit != null && importEdit.hasChildren()) {
                editRoot.addChild(importEdit);
            }
        }
    }

    /**
     * Not used — {@link #addEdits} handles everything directly to avoid
     * ASTRewrite producing delete ranges that cross line boundaries.
     */
    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        return null;
    }

    /**
     * Returns the Compilation Unit node
     *
     * @return the invocation node for the Compilation Unit
     */
    protected CompilationUnit getInvocationNode() {
        return this.fInvocationNode;
    }

    /**
     * Returns the Binding object associated with the new annotation change
     *
     * @return the binding object
     */
    protected IBinding getBinding() {
        return this.fBinding;
    }

    /**
     * Returns the annotations list
     *
     * @return the list of new annotations to add
     */
    protected String[] getAnnotations() {
        return this.annotations;
    }

    private static boolean matchesAnnotation(String fqn, String typeName) {
        return fqn.equals(typeName) || fqn.endsWith("." + typeName);
    }
}
