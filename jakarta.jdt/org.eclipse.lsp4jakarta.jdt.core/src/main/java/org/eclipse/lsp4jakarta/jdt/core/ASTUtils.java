/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Yijia Jing
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ASTUtils {

    /**
     * Converts a given compilation unit to an ASTNode.
     *
     * @param unit
     * @return ASTNode parsed from the compilation unit
     */
    public static ASTNode getASTNode(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return parser.createAST(null);
    }

    /**
     * Given a compilation unit returns a list of all method invocations.
     *
     * @param unit
     * @return list of method invocations
     */
    public static List<MethodInvocation> getMethodInvocations(ICompilationUnit unit) {
        ASTNode node = getASTNode(unit);
        MethodInvocationVisitor visitor = new ASTUtils().new MethodInvocationVisitor();
        node.accept(visitor);
        return visitor.getMethodInvocations();
    }

    /**
     * This visitor visits an ASTNode and records all the method invocations during its visit.
     */
    public class MethodInvocationVisitor extends ASTVisitor {
        private final List<MethodInvocation> invocations = new ArrayList<>();

        @Override
        public boolean visit(final MethodInvocation m) {
            invocations.add(m);
            return super.visit(m);
        }

        public List<MethodInvocation> getMethodInvocations() {
            return Collections.unmodifiableList(invocations);
        }
    }

    /**
     * Retrieves the enclosing method declaration for a given AST node.
     *
     * <p>This method traverses up the Abstract Syntax Tree (AST) hierarchy starting from the
     * given node, searching for the nearest ancestor that is a {@link MethodDeclaration}.
     * This is useful for determining the method context in which a particular AST node exists.</p>
     *
     * @param node the AST node for which to find the enclosing method declaration
     * @return the nearest enclosing {@link MethodDeclaration}, or {@code null} if the node
     *         is not contained within any method (e.g., if it's a field declaration or
     *         class-level element)
     */
    public static MethodDeclaration getEnclosingMethod(ASTNode node) {
        ASTNode currentNode = node.getParent();
        while (currentNode != null) {
            if (currentNode instanceof MethodDeclaration) {
                return (MethodDeclaration) currentNode;
            }
            currentNode = currentNode.getParent();
        }
        return null;
    }

    /**
     * Resolves the fully qualified class name of the declaring class for a method invocation.
     *
     * @param mi the method invocation
     * @return the fully qualified class name, or null if it cannot be resolved
     */
    public static String getDeclaringClassName(MethodInvocation mi) {
        IMethodBinding binding = mi.resolveMethodBinding();
        if (binding == null) {
            return null;
        }
        ITypeBinding declaringClass = binding.getDeclaringClass();
        if (declaringClass == null) {
            return null;
        }
        return declaringClass.getQualifiedName();
    }
}
