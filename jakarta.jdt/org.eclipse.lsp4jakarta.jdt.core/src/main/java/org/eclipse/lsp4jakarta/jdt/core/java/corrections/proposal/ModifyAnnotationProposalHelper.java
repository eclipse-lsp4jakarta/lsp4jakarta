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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;

/**
 * ModifyAnnotationProposalHelper
 * Helper class for handling utility methods of the ModifyAnnotationProposal
 */
public class ModifyAnnotationProposalHelper {



	
    /**
     * findAttributeMethod
     * Returns the annotation attribute method matching the given name
     *
     * @param annotationBinding
     * @param attrName
     * @return
     */
    public IMethodBinding findAttributeMethod(ITypeBinding annotationBinding, String attrName) {
        if (annotationBinding == null) {
            return null;
        }
        for (IMethodBinding method : annotationBinding.getDeclaredMethods()) {
            if (method.getName().equals(attrName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * createDefaultValue
     * Creates a default AST {@link Expression} for the given {@link ITypeBinding}.
     *
     * @param ast
     * @param typeBinding
     * @return
     */
    public Expression createDefaultValue(AST ast,
                                         ITypeBinding typeBinding) {
        if (typeBinding == null) {
            return ast.newNullLiteral();

        }
        if (typeBinding.isArray()) {
            ArrayInitializer initializer = ast.newArrayInitializer();
            return initializer;
        }

        if (typeBinding.isPrimitive()) {
            String name = typeBinding.getName();
            switch (name) {
                case "boolean":
                    return ast.newBooleanLiteral(false);
                case "long":
                    return ast.newNumberLiteral("0L");
                case "float":
                    return ast.newNumberLiteral("0f");
                case "double":
                    return ast.newNumberLiteral("0d");
                case "char":
                    CharacterLiteral ch = ast.newCharacterLiteral();
                    ch.setCharValue('\0');
                    return ch;
                default:
                    return ast.newNumberLiteral("0");
            }
        }
        String qualifiedName = typeBinding.getQualifiedName();
        if ("java.lang.String".equals(qualifiedName)) {
            StringLiteral literal = ast.newStringLiteral();
            literal.setLiteralValue("");
            return literal;
        }
        if ("java.lang.Class".equals(typeBinding.getErasure().getQualifiedName())) {
            TypeLiteral typeLiteral = ast.newTypeLiteral();
            typeLiteral.setType(ast.newSimpleType(ast.newSimpleName("Object")));
            return typeLiteral;
        }
        return ast.newNullLiteral();

    }

    /**
     * convertObjectToExpression
     * Converts a plain object into an AST {@link Expression} based on its associated type.
     *
     * @param ast
     * @param defaultVal
     * @return
     */
    public Expression convertObjectToExpression(AST ast, Object defaultVal) {

        if (defaultVal instanceof Boolean) {
            return ast.newBooleanLiteral((Boolean) defaultVal);
        }
        if (defaultVal instanceof Byte || defaultVal instanceof Short || defaultVal instanceof Integer
            || defaultVal instanceof Long || defaultVal instanceof Float || defaultVal instanceof Double) {
            return ast.newNumberLiteral(defaultVal.toString());
        }
        if (defaultVal instanceof Character) {
            CharacterLiteral ch = ast.newCharacterLiteral();
            ch.setCharValue((Character) defaultVal);
            return ch;
        }
        if (defaultVal instanceof String) {
            StringLiteral str = ast.newStringLiteral();
            str.setLiteralValue((String) defaultVal);
            return str;
        }
        if (defaultVal instanceof Object[]) {
            ArrayInitializer arr = ast.newArrayInitializer();
            for (Object element : (Object[]) defaultVal) {
                arr.expressions().add(convertObjectToExpression(ast, element));
            }
            return arr;
        }
        if (defaultVal instanceof IVariableBinding) {
            IVariableBinding var = (IVariableBinding) defaultVal;
            if (var.isEnumConstant()) {
                ITypeBinding declaringClass = var.getDeclaringClass();
                Name enumTypeName = ast.newName(declaringClass.getName());
                return ast.newQualifiedName(enumTypeName, ast.newSimpleName(var.getName()));
            }
        }
        if (defaultVal instanceof ITypeBinding) {
            ITypeBinding typeBinding = (ITypeBinding) defaultVal;
            TypeLiteral typeLiteral = ast.newTypeLiteral();
            typeLiteral.setType(ast.newSimpleType(ast.newSimpleName(typeBinding.getName())));
            return typeLiteral;
        }

        // Fallback
        return ast.newNullLiteral();
    }
}