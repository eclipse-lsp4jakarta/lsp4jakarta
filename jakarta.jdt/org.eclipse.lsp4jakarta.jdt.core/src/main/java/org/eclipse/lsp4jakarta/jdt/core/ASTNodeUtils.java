/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Utility methods for working with {@link ASTNode}.
 */
public class ASTNodeUtils {

    private ASTNodeUtils() {}

    /**
     * Returns true if the given <code>ASTNode</code> represents an annotation, and false otherwise.
     *
     * @param node the ast node to check, assumed to be non-null
     * @return true if the given <code>ASTNode</code> represents an annotation, and false otherwise
     */
    public static boolean isAnnotation(ASTNode node) {
        int nodeType = node.getNodeType();
        return nodeType == ASTNode.MARKER_ANNOTATION || nodeType == ASTNode.SINGLE_MEMBER_ANNOTATION
               || nodeType == ASTNode.NORMAL_ANNOTATION;
    }

    /**
     * Returns the binding of the parent type (method, field, or type declaration) for the given AST node.
     * This method walks up the AST tree to find the nearest enclosing declaration and returns its binding.
     * <p>
     * This is a replacement for the internal API {@code Bindings.getBindingOfParentType(ASTNode)} which
     * should not be used by external code.
     * </p>
     *
     * @param node the AST node to get the parent binding for
     * @return the binding of the parent declaration, or null if no binding can be resolved
     */
    public static IBinding getParentTypeBinding(ASTNode node) {
        if (node == null) {
            return null;
        }

        // Check if parent is a VariableDeclarationFragment (field)
        if (node.getParent() instanceof VariableDeclarationFragment) {
            return ((VariableDeclarationFragment) node.getParent()).resolveBinding();
        }

        // Walk up the AST to find the method, field, or type declaration
        ASTNode current = node;
        while (current != null) {
            if (current instanceof MethodDeclaration) {
                return ((MethodDeclaration) current).resolveBinding();
            } else if (current instanceof FieldDeclaration) {
                // For field declarations, we need to get the variable binding
                FieldDeclaration fieldDecl = (FieldDeclaration) current;
                if (!fieldDecl.fragments().isEmpty()) {
                    return ((VariableDeclarationFragment) fieldDecl.fragments().get(0)).resolveBinding();
                }
            } else if (current instanceof TypeDeclaration) {
                return ((TypeDeclaration) current).resolveBinding();
            }
            current = current.getParent();
        }

        // Return null if no binding can be resolved through standard API
        return null;
    }

}
