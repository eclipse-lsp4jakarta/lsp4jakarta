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
    public class MethodDeclarationVisitor extends ASTVisitor {
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
}
