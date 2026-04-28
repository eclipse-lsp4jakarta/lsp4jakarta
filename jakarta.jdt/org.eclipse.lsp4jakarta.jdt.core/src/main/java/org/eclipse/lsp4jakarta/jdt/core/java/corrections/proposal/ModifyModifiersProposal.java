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
 *     IBM Corporation, Matthew Shocrylas - initial API and implementation
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4jakarta.jdt.internal.jaxrs.InsertDefaultPublicConstructorQuickFix;

/**
 * Code action proposal for changing the visibility modifier of a method, field,
 * or type. Used by JAX-RS NonPublicResourceMethodQuickFix.
 *
 * @author Matthew Shocrylas
 * @author Leslie Dawson
 * @see CodeActionHandler
 * @see InsertDefaultPublicConstructorQuickFix
 * @see PersistenceEntityQuickFix
 *
 */
public class ModifyModifiersProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit invocationNode;
    private final IBinding binding;
    private final ASTNode coveredNode;

    // list of modifiers to add
    private final List<String> modifiersToAdd;

    // list of modifiers (if they exist) to remove
    private final List<String> modifiersToRemove;

    /**
     * Constructor for ModifyModifiersProposal that accepts both a list of modifiers
     * to remove as well as to add
     *
     * @param modifiersToAdd list of valid modifiers as strings to be added
     * @param modifiersToRemove list of modifiers as strings to be removed
     */
    public ModifyModifiersProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                   IBinding binding, int relevance, ASTNode coveredNode, List<String> modifiersToAdd,
                                   List<String> modifiersToRemove) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.binding = binding;
        this.coveredNode = coveredNode;
        this.modifiersToAdd = modifiersToAdd;
        this.modifiersToRemove = modifiersToRemove;
    }

    /**
     * Constructor for ModifyModifiersProposal that accepts only a list of modifiers
     * to add If a visibility modifier is specified to be added, existing visibility
     * modifiers will be removed
     *
     * @param modifiersToAdd list of valid modifiers as strings to be added
     */
    public ModifyModifiersProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode,
                                   IBinding binding, int relevance, ASTNode coveredNode, List<String> modifiersToAdd) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.binding = binding;
        this.coveredNode = coveredNode;
        this.modifiersToAdd = modifiersToAdd;
        this.modifiersToRemove = new ArrayList<>();
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        ASTNode declNode = null;
        ASTNode boundNode = invocationNode.findDeclaringNode(binding);
        CompilationUnit newRoot = invocationNode;

        if (boundNode != null) {
            declNode = boundNode;
        } else {
            newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
            declNode = newRoot.findDeclaringNode(binding.getKey());
        }

        if (coveredNode != null) {
            declNode = coveredNode;
        }
        boolean isField = declNode instanceof VariableDeclarationFragment;
        if (isField) {
            declNode = declNode.getParent();
        }

        AST ast = declNode.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        ListRewrite modifiersList = null;
        List<ASTNode> modifiers = new ArrayList();

        switch (declNode.getNodeType()) {
            case ASTNode.METHOD_DECLARATION:
                modifiersList = rewrite.getListRewrite(declNode, MethodDeclaration.MODIFIERS2_PROPERTY);
                modifiers = (List<ASTNode>) declNode.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);
                break;
            case ASTNode.FIELD_DECLARATION:
                modifiersList = rewrite.getListRewrite(declNode, FieldDeclaration.MODIFIERS2_PROPERTY);
                modifiers = (List<ASTNode>) declNode.getStructuralProperty(FieldDeclaration.MODIFIERS2_PROPERTY);
                break;
            case ASTNode.TYPE_DECLARATION:
                modifiersList = rewrite.getListRewrite(declNode, TypeDeclaration.MODIFIERS2_PROPERTY);
                modifiers = (List<ASTNode>) declNode.getStructuralProperty(TypeDeclaration.MODIFIERS2_PROPERTY);
                break;
            case ASTNode.SINGLE_VARIABLE_DECLARATION:
                modifiersList = rewrite.getListRewrite(declNode, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
                modifiers = (List<ASTNode>) declNode.getStructuralProperty(SingleVariableDeclaration.MODIFIERS2_PROPERTY);
                break;
            default:
                modifiersList = null;
        }

        for (ASTNode modifier : modifiers) {
            if (modifier instanceof Annotation) {
                Annotation annotation = (Annotation) modifier;
                Name annoTypeName = annotation.getTypeName();
                // Check if modifier is in toRemove list - compare simple name
                if (modifiersToRemove.stream().anyMatch(m -> m.equals(annoTypeName.toString()))) {
                    modifiersList.remove(annotation, null);
                    continue;
                }
                // Also check against fully qualified name if type binding is available
                ITypeBinding typeBinding = annotation.resolveTypeBinding();
                if (typeBinding != null && modifiersToRemove.stream().anyMatch(m -> m.equals(typeBinding.getQualifiedName()))) {
                    modifiersList.remove(annotation, null);
                    continue;
                }
            } else if (modifier instanceof Modifier) {
                Modifier.ModifierKeyword modKeyword = ((Modifier) modifier).getKeyword();

                // Check if modifier is in toRemove list
                if (modifiersToRemove.stream().anyMatch(m -> m.equals(modKeyword.toString()))) {
                    modifiersList.remove(modifier, null);
                    continue;
                }

                // Otherwise check if the existing modifier is private, public or protected
                if (modKeyword.equals(Modifier.ModifierKeyword.PRIVATE_KEYWORD)
                    || modKeyword.equals(Modifier.ModifierKeyword.PUBLIC_KEYWORD)
                    || modKeyword.equals(Modifier.ModifierKeyword.PROTECTED_KEYWORD)) {

                    // if adding a visibility modifier, need to remove the existing one
                    if (modifiersToAdd.stream().anyMatch(m -> (m.equals(Modifier.ModifierKeyword.PRIVATE_KEYWORD.toString())
                                                               || m.equals(Modifier.ModifierKeyword.PUBLIC_KEYWORD.toString())
                                                               || m.equals(Modifier.ModifierKeyword.PROTECTED_KEYWORD.toString())))) {
                        modifiersList.remove(modifier, null);
                    }
                }
            }
        }

        // now add the ones we want
        for (String newModifier : modifiersToAdd) {
            // make a new marker
            Modifier marker = ast.newModifier(Modifier.ModifierKeyword.toKeyword(newModifier));
            modifiersList.insertLast(marker, null);
        }

        return rewrite;
    }
}
