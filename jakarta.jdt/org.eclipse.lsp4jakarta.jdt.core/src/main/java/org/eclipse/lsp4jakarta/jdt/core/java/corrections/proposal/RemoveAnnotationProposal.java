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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

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
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
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
    private static final Logger LOGGER = Logger.getLogger(RemoveAnnotationProposal.class.getName());

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
        // Do not call super.addEdits() — we handle all edits and import rewrites
        // directly, bypassing the ASTRewrite machinery in the parent class.
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
        // Detect the actual line delimiter used in this document (\n or \r\n).
        String lineDelim = TextUtilities.getDefaultLineDelimiter(document);
        char lineDelimEnd = lineDelim.charAt(lineDelim.length() - 1); // always '\n'
        List<int[]> pendingDeletes = new ArrayList<>();

        for (int i = 0; i < children.size(); i++) {
            ASTNode child = children.get(i);
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
            if (binding == null || !binding.getQualifiedName().equals(matchingFqn)) {
                continue;
            }

            // Finding line boundaries
            int annotStart = child.getStartPosition(); // where @ starts
            int annotEnd = annotStart + child.getLength(); // where annotation ends

            // Find the start of the line containing this annotation.
            int lineStart = annotStart;
            while (lineStart > 0 && source.charAt(lineStart - 1) != lineDelimEnd) {
                lineStart--;
            }

            // Find the end of the line containing this annotation (exclusive of line delimiter).
            int lineEnd = annotEnd;
            while (lineEnd < source.length() && source.charAt(lineEnd) != lineDelimEnd) {
                lineEnd++;
            }

            // Content before and after the annotation on the same line.
            String before = source.substring(lineStart, annotStart);
            String after = source.substring(annotEnd, lineEnd);

            boolean atLineEnd = after.trim().isEmpty();

            int deleteStart;
            int deleteEnd;

            if (!atLineEnd) {
                // Case C: annotation shares its line with tokens that follow it
                // (e.g. an inline annotation in a parameter list, or a leading
                // annotation when siblings follow on the same line).
                // Delete the annotation plus any immediately-following whitespace so
                // the next token slides into place without a double-space.
                deleteStart = annotStart;
                deleteEnd = annotEnd;
                while (deleteEnd < source.length()
                        && (source.charAt(deleteEnd) == ' ' || source.charAt(deleteEnd) == '\t')) {
                    deleteEnd++;
                }
            } else if (!before.trim().isEmpty()) {
                // Case A: annotation is the last non-whitespace token on its line AND
                // there are other non-whitespace tokens before it on the SAME line.
                // Delete only the annotation (and any leading space absorbed by annotStart)
                // up to but NOT including the \n, so the line break is preserved and the
                // next line stays on its own line.
                deleteStart = annotStart;
                deleteEnd = lineEnd;   // stop before the \n — preserve the line break
            } else {
                // Case B: annotation is alone on its line (only whitespace before/after).
                //
                // ASTRewrite absorbs the preceding newline (backward) when ALL of:
                //   - the declaring node is a field or type (not a method or parameter),
                //   - this annotation is the last element in the modifiers list,
                //   - the preceding line ends with non-whitespace content,
                //   - no other annotation has been queued for deletion yet (sole removal).
                //
                // Otherwise absorb the following newline and next-line indent (forward).
                int prevLineEnd = lineStart - 1; // offset of the line delimiter before this line, or -1
                boolean prevLineHasContent = false;
                if (prevLineEnd >= 0) {
                    // scan backward from line delimiter to the start of the preceding line
                    int scan = prevLineEnd - 1;
                    while (scan >= 0 && source.charAt(scan) != lineDelimEnd) {
                        if (!Character.isWhitespace(source.charAt(scan))) {
                            prevLineHasContent = true;
                            break;
                        }
                        scan--;
                    }
                    // If the preceding line is an annotation or a comment, do not absorb
                    // backward — absorbing into an annotation would corrupt it, and
                    // absorbing into a comment would produce confusing output.
                    if (prevLineHasContent) {
                        int prevLineStart = prevLineEnd - 1;
                        while (prevLineStart > 0 && source.charAt(prevLineStart - 1) != lineDelimEnd) {
                            prevLineStart--;
                        }
                        String prevLineContent = source.substring(prevLineStart, prevLineEnd).stripLeading();
                        if (prevLineContent.startsWith("@")
                                || prevLineContent.startsWith("//")
                                || prevLineContent.startsWith("/*")
                                || prevLineContent.startsWith("*")) {
                            prevLineHasContent = false;
                        }
                    }
                }

                // Backward is appropriate only when this is the sole annotation being
                // removed in this call (i.e. no other delete has been queued yet).
                // When multiple annotations are removed together, forward absorption
                // for all of them produces adjacent ranges that merge cleanly.
                boolean useBackward = (isField || isType)
                        && i == children.size() - 1
                        && prevLineHasContent
                        && pendingDeletes.isEmpty();

                if (useBackward) {
                    // Absorb backward: delete '\n' + annotation (preceding line stays intact)
                    deleteStart = prevLineEnd; // the '\n' ending the preceding line
                    deleteEnd = annotEnd;
                } else {
                    // Absorb forward: delete annotation + line delimiter + next-line indent
                    deleteStart = annotStart;
                    int nextLineStart = (lineEnd < source.length() && source.charAt(lineEnd) == lineDelimEnd)
                            ? lineEnd + 1 : lineEnd;
                    int nextLineIndent = 0;
                    while (nextLineStart + nextLineIndent < source.length()
                            && (source.charAt(nextLineStart + nextLineIndent) == ' '
                                    || source.charAt(nextLineStart + nextLineIndent) == '\t')) {
                        nextLineIndent++;
                    }
                    deleteEnd = nextLineStart + nextLineIndent;
                }
            }

            pendingDeletes.add(new int[]{deleteStart, deleteEnd});
        }

        if (pendingDeletes.isEmpty()) {
            LOGGER.warning("RemoveAnnotationProposal: no matching annotations found to remove for "
                    + Arrays.toString(annotations) + " on node " + declaringNode.getClass().getSimpleName());
        }

        // Merge overlapping or adjacent DeleteEdits so that removing multiple
        // consecutive annotations produces a single, non-overlapping edit.
        pendingDeletes.sort((a, b) -> Integer.compare(a[0], b[0]));
        int[] current = null;
        for (int[] range : pendingDeletes) {
            if (current == null) {
                current = new int[]{range[0], range[1]};
            } else if (range[0] <= current[1]) {
                // overlapping or adjacent — extend
                current[1] = Math.max(current[1], range[1]);
            } else {
                editRoot.addChild(new DeleteEdit(current[0], current[1] - current[0]));
                current = new int[]{range[0], range[1]};
            }
        }
        if (current != null) {
            editRoot.addChild(new DeleteEdit(current[0], current[1] - current[0]));
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
