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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;

/**
 * ModifyAnnotationProposalHelper
 */
public class ModifyAnnotationProposalHelper {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(ModifyAnnotationProposalHelper.class.getName());

    /**
     * findDefaultAttributeValue
     * Returns the default AST {@link Expression} for an annotation attribute.
     * If the attribute has a declared default, that is used. Otherwise, a
     * custom default is created based on the attribute type.
     *
     * @param annotationToProcess
     * @param attrName
     * @param ast
     * @param iJavaProject
     * @param annotationFqn
     * @return
     */
    public static Expression findDefaultAttributeValue(NormalAnnotation annotationToProcess,
                                                       String attrName,
                                                       AST ast,
                                                       IJavaProject iJavaProject,
                                                       String annotationFqn) {
        ITypeBinding annotationBinding = null;
        if (null != annotationToProcess) {
            annotationBinding = annotationToProcess.resolveTypeBinding();
        }

        // Case 1: new annotation (binding not yet available)
        if (annotationBinding == null) {
            try {
                IType annotationType = iJavaProject.findType(annotationFqn);
                return createCustomDefaultValue(annotationType, attrName, ast);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to create Default Attribute Value", e);
                return ast.newNullLiteral();
            }
        }

        // Case 2: existing annotation with binding
        for (IMethodBinding method : annotationBinding.getDeclaredMethods()) {
            if (method.getName().equals(attrName)) {
                Object defaultVal = method.getDefaultValue();
                if (defaultVal != null) {
                    return convertObjectToExpression(ast, defaultVal);
                } else {
                    return createCustomDefaultValue(ast, method.getReturnType());
                }
            }
        }
        LOGGER.log(Level.WARNING, "Unable to create Default Attribute Value");
        return ast.newNullLiteral();
    }

    /**
     * createCustomDefaultValue
     * Finds an annotation attribute by name and returns a custom default
     * value expression based on its return type.
     *
     * @param annotationType
     * @param attributeName
     * @param ast
     * @return
     * @throws Exception
     */
    private static Expression createCustomDefaultValue(IType annotationType,
                                                       String attributeName,
                                                       AST ast) throws Exception {
        for (IMethod method : annotationType.getMethods()) {
            if (method.getElementName().equals(attributeName)) {
                String sig = method.getReturnType();
                String readableType = Signature.toString(sig);
                return createDefaultValueForType(readableType, ast);
            }
        }
        LOGGER.log(Level.WARNING, "Unable to create Default Attribute Value");
        return ast.newNullLiteral();
    }

    /**
     * createCustomDefaultValue
     * Creates a custom default value expression for a given type binding.
     *
     * @param ast
     * @param typeBinding
     * @return
     */
    private static Expression createCustomDefaultValue(AST ast, ITypeBinding typeBinding) {
        if (typeBinding == null) {
            return ast.newNullLiteral();
        }
        if (typeBinding.isArray()) {
            return ast.newArrayInitializer();
        }
        if (typeBinding.isEnum()) {
            for (IVariableBinding field : typeBinding.getDeclaredFields()) {
                if (field.isEnumConstant()) {
                    // Use the first enum constant as a default value
                    return ast.newQualifiedName(
                                                ast.newSimpleName(typeBinding.getName()),
                                                ast.newSimpleName(field.getName()));
                }
            }
        }

        return createDefaultValueForType(typeBinding.getQualifiedName(), ast);
    }

    /**
     * convertObjectToExpression
     * Converts a plain object into an AST {@link Expression}.
     *
     * @param ast
     * @param defaultVal
     * @return
     */
    private static Expression convertObjectToExpression(AST ast, Object defaultVal) {
        if (defaultVal instanceof Boolean) {
            return ast.newBooleanLiteral((Boolean) defaultVal);
        }
        if (defaultVal instanceof Number) {
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
                Name enumTypeName = ast.newName(var.getDeclaringClass().getName());
                return ast.newQualifiedName(enumTypeName, ast.newSimpleName(var.getName()));
            }
        }
        if (defaultVal instanceof ITypeBinding) {
            TypeLiteral typeLiteral = ast.newTypeLiteral();
            typeLiteral.setType(ast.newSimpleType(ast.newSimpleName(((ITypeBinding) defaultVal).getName())));
            return typeLiteral;
        }
        LOGGER.log(Level.WARNING, "Unable to create Default Attribute Value");
        return ast.newNullLiteral();
    }

    /**
     * createDefaultValueForType
     * Creates a synthetic default value expression based on a type name string.
     *
     * @param typeName
     * @param ast
     * @return
     */
    private static Expression createDefaultValueForType(String typeName, AST ast) {
        switch (typeName) {
            case "byte":
            case "short":
            case "int":
                return ast.newNumberLiteral("0");
            case "long":
                return ast.newNumberLiteral("0L");
            case "float":
                return ast.newNumberLiteral("0f");
            case "double":
                return ast.newNumberLiteral("0d");
            case "boolean":
                return ast.newBooleanLiteral(false);
            case "char":
                CharacterLiteral ch = ast.newCharacterLiteral();
                ch.setCharValue('\0');
                return ch;
            case "java.lang.String":
                StringLiteral str = ast.newStringLiteral();
                str.setLiteralValue("");
                return str;
            default:
                // Handle Class types (java.lang.Class, Class<?>, Class<? extends Foo>)
                if (typeName.startsWith("java.lang.Class") || typeName.startsWith("Class")) {
                    TypeLiteral typeLiteral = ast.newTypeLiteral();
                    typeLiteral.setType(ast.newSimpleType(ast.newSimpleName("Object")));
                    return typeLiteral;
                }
                // Handle arrays (including multi-dimensional and generic arrays)
                if (typeName.endsWith("[]")) {
                    return ast.newArrayInitializer();
                }
                LOGGER.log(Level.WARNING, "Unable to create Default Attribute Value");
                return ast.newNullLiteral();
        }
    }
}
