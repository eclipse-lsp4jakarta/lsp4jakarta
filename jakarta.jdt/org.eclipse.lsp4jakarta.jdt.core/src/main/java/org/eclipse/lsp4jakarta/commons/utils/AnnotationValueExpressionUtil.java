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
package org.eclipse.lsp4jakarta.commons.utils;

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
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;

/**
 * Factory for creating annotation attribute value expressions.
 * Supports:
 * <ul>
 * <li>Numeric values: pass a {@link Number} instance (int, long, float, double)</li>
 * <li>Boolean values: pass a {@link Boolean} instance</li>
 * <li>String literals: pass a {@link String} wrapped in double quotes, e.g., {@code "\"abc.def\""}</li>
 * <li>Qualified names (enum constants): pass a {@link String}, e.g., {@code "TemporalType.DATE"}</li>
 * <li>Simple names: pass a {@link String}, e.g., {@code "DATE"}</li>
 * </ul>
 */
public class AnnotationValueExpressionUtil {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(AnnotationValueExpressionUtil.class.getName());

    /**
     * Creates an expression for the annotation attribute value.
     * Accepts any of the following types:
     * <ul>
     * <li>{@link Number} - creates a numeric literal (int, long, float, double)</li>
     * <li>{@link Boolean} - creates a boolean literal</li>
     * <li>{@link String} wrapped in double quotes - creates a string literal</li>
     * <li>{@link String} with a dot - creates a qualified name (enum constant)</li>
     * <li>{@link String} - creates a simple name</li>
     * </ul>
     *
     * @param ast The AST
     * @param value The attribute value (String, Number, or Boolean)
     * @param annotation The fully qualified annotation name (used to infer package for qualified names)
     * @param imports The import rewrite
     * @param importRewriteContext The import rewrite context
     * @return The expression for the value
     */
    public static Expression createValueExpression(AST ast, Object value, String annotation,
                                                   ImportRewrite imports, ImportRewriteContext importRewriteContext) {

        if (value instanceof String) {
            String doubleQuote = "\"";
            String dot = ".";
            String strValue = value.toString();
            // Check if it's a string literal (wrapped in quotes)
            if (strValue.startsWith(doubleQuote) && strValue.endsWith(doubleQuote)) {
                // Remove the quotes and create a string literal
                String literalValue = strValue.substring(1, strValue.length() - 1);
                StringLiteral stringLiteral = ast.newStringLiteral();
                stringLiteral.setLiteralValue(literalValue);
                return stringLiteral;
            }

            // Check if it's a qualified enum name (e.g., enum constant like TemporalType.DATE)
            if (strValue.contains(dot)) {
                String[] parts = strValue.split("\\" + dot);
                String typeName = parts[0]; // e.g., "TemporalType"
                String fieldName = parts[1]; // e.g., "DATE"

                // Infer the package from the annotation package
                String annotationPackage = annotation.substring(0, annotation.lastIndexOf(dot));
                String fullyQualifiedTypeName = annotationPackage + dot + typeName;

                // Add import for the type
                String importedTypeName = imports.addImport(fullyQualifiedTypeName, importRewriteContext);

                // Create qualified name using the imported type name
                return ast.newQualifiedName(
                                            ast.newSimpleName(importedTypeName),
                                            ast.newSimpleName(fieldName));
            }
        } else {
            return convertObjectToExpression(ast, value);
        }

        logUnableToCreateDefaultValue();
        return ast.newNullLiteral();

    }

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
                logUnableToCreateDefaultValue();
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
        logUnableToCreateDefaultValue();
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
        logUnableToCreateDefaultValue();
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
        logUnableToCreateDefaultValue();
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
                logUnableToCreateDefaultValue();
                return ast.newNullLiteral();
        }
    }

    /**
     * log if Unable To Create DefaultValue
     */
    private static void logUnableToCreateDefaultValue() {
        LOGGER.log(Level.WARNING, "Unable to create Default Attribute Value");
    }
}
