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
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeActionKind;

/**
 * Generic AST rewrite proposal that replaces a method's parameter list so it
 * contains exactly one parameter of a caller-specified primitive type.
 *
 * <p>The four cases handled automatically:
 * <ol>
 * <li><b>No params</b> &rarr; inserts one parameter of the required type
 * with the caller-supplied default name</li>
 * <li><b>Single param, wrong type</b> &rarr; changes its type to the
 * required type (name kept as-is)</li>
 * <li><b>Multiple params, one already the required type</b> &rarr; removes
 * all others; keeps the matching one</li>
 * <li><b>Multiple params, none the required type</b> &rarr; keeps the first
 * param but changes its type; removes all others</li>
 * </ol>
 *
 * <p>This proposal is annotation-agnostic. Callers supply the
 * {@link PrimitiveType.Code} and default parameter name to use.
 */
public class ReplaceParametersWithSingleParamProposal extends ASTRewriteCorrectionProposal {

    private final CompilationUnit invocationNode;
    private final IBinding binding;
    private final PrimitiveType.Code requiredPrimitiveCode;
    private final String defaultParamName;

    /**
     * Constructor.
     *
     * @param label the code action label shown to the user
     * @param targetCU the compilation unit to modify
     * @param invocationNode the parsed compilation unit root
     * @param binding the binding of the target method declaration
     * @param relevance the proposal relevance
     * @param requiredPrimitive the primitive type code the single parameter must have
     * @param defaultParamName the name used when inserting a brand-new parameter
     */
    public ReplaceParametersWithSingleParamProposal(String label, ICompilationUnit targetCU,
                                                    CompilationUnit invocationNode, IBinding binding,
                                                    int relevance,
                                                    PrimitiveType.Code requiredPrimitive,
                                                    String defaultParamName) {
        super(label, CodeActionKind.QuickFix, targetCU, null, relevance);
        this.invocationNode = invocationNode;
        this.binding = binding;
        this.requiredPrimitiveCode = requiredPrimitive;
        this.defaultParamName = defaultParamName;
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        ASTNode declNode = invocationNode.findDeclaringNode(binding);
        if (declNode == null) {
            CompilationUnit newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
            declNode = newRoot.findDeclaringNode(binding.getKey());
        }

        if (!(declNode instanceof MethodDeclaration method)) {
            return null;
        }

        AST ast = method.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);

        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> params = (List<SingleVariableDeclaration>) method.parameters();

        ListRewrite paramList = rewrite.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);

        if (params.isEmpty()) {
            // Case 1: no params → insert one parameter of the required type
            SingleVariableDeclaration newParam = ast.newSingleVariableDeclaration();
            newParam.setType(ast.newPrimitiveType(requiredPrimitiveCode));
            newParam.setName(ast.newSimpleName(defaultParamName));
            paramList.insertFirst(newParam, null);

        } else if (params.size() == 1) {
            // Case 2: single param with wrong type → change its type (name kept)
            rewrite.set(params.get(0), SingleVariableDeclaration.TYPE_PROPERTY,
                        ast.newPrimitiveType(requiredPrimitiveCode), null);

        } else {
            OptionalInt matchIndex = findMatchingParamIndex(params);

            if (matchIndex.isPresent()) {
                // Case 3: one already has the required type → remove all others
                int keepIdx = matchIndex.getAsInt();
                IntStream.range(0, params.size()).filter(i -> i != keepIdx).mapToObj(params::get).forEach(p -> paramList.remove(p, null));
            } else {
                // Case 4: none has the required type → keep first, change its type; remove rest
                rewrite.set(params.get(0), SingleVariableDeclaration.TYPE_PROPERTY,
                            ast.newPrimitiveType(requiredPrimitiveCode), null);
                params.stream().skip(1).forEach(p -> paramList.remove(p, null));
            }
        }

        return rewrite;
    }

    /**
     * Returns the index of the first parameter whose type already matches the
     * required primitive type, or an empty {@link OptionalInt} if none matches.
     *
     * @param params the method's parameter list
     * @return an {@link OptionalInt} containing the zero-based index of the first
     *         matching parameter, or empty if none is found
     */
    private OptionalInt findMatchingParamIndex(List<SingleVariableDeclaration> params) {
        return IntStream.range(0, params.size()).filter(i -> {
            SingleVariableDeclaration p = params.get(i);
            return p.getType().isPrimitiveType()
                   && ((PrimitiveType) p.getType()).getPrimitiveTypeCode() == requiredPrimitiveCode;
        }).findFirst();
    }
}
