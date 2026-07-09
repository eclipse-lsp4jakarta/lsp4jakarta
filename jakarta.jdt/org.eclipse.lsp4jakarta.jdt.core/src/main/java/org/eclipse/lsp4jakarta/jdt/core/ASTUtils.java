/*******************************************************************************
 * Copyright (c) 2022, 2026 IBM Corporation and others.
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
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodDeclaration;

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
     * This visitor visits an ASTNode and records all the method declarations during its visit.
     */
    private class MethodDeclarationVisitor extends ASTVisitor {
        private final List<MethodDeclaration> declarations = new ArrayList<>();

        @Override
        public boolean visit(final MethodDeclaration m) {
            declarations.add(m);
            return super.visit(m);
        }

        public List<MethodDeclaration> getMethodDeclarations() {
            return Collections.unmodifiableList(declarations);
        }
    }

    /**
     * Given a compilation unit returns a list of all method declarations.
     *
     * @param unit
     * @return list of method declarations
     */
    public static List<MethodDeclaration> getMethodDeclarations(ICompilationUnit unit) {
        ASTNode node = getASTNode(unit);
        MethodDeclarationVisitor visitor = new ASTUtils().new MethodDeclarationVisitor();
        node.accept(visitor);
        return visitor.getMethodDeclarations();
    }

    /**
     * Checks whether the given MethodDeclaration contains a call to the specified method
     * on the specified parent type. Does that by getting Method invocations specific to the method.
     *
     * @param methodDecl
     * @param targetMethod
     * @param parentFQN
     * @return boolean
     */
    public static boolean containsMethodInvocation(MethodDeclaration methodDecl, String targetMethod, String parentFQN) {
        if (methodDecl == null || methodDecl.getBody() == null) {
            return false;
        }
        AtomicBoolean found = new AtomicBoolean(false);
        methodDecl.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                if (found.get())
                    return false; // stop descending if found
                if (targetMethod.equals(node.getName().getIdentifier())) {
                    IMethodBinding binding = node.resolveMethodBinding();
                    if (binding != null) {
                        ITypeBinding declaringClass = binding.getDeclaringClass();
                        if (declaringClass != null &&
                            parentFQN.equals(declaringClass.getQualifiedName())) {
                            found.set(true);
                            return false; // stop visiting children of this node
                        }
                    }
                }
                return true; // keep traversing nodes until found
            }
        });
        return found.get();
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

    /**
     * Checks if the declaring class name of a method invocation matches the expected fully qualified name.
     *
     * @param mi the method invocation
     * @param expectedFQN the expected fully qualified class name to match against
     * @return true if the declaring class matches the expected FQN, false otherwise
     */
    public static boolean isMatchedTargetClass(MethodInvocation mi, String expectedFQN) {
        String qualifiedName = getDeclaringClassName(mi);
        return expectedFQN.equals(qualifiedName);
    }
}