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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;
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
        // Handle Number types directly
        if (value instanceof Number) {
            return ast.newNumberLiteral(value.toString());
        }

        // Handle Boolean types directly
        if (value instanceof Boolean) {
            return ast.newBooleanLiteral((Boolean) value);
        }

        // Handle String values
        String strValue = value.toString();

        // Check if it's a string literal (wrapped in quotes)
        if (strValue.startsWith("\"") && strValue.endsWith("\"")) {
            // Remove the quotes and create a string literal
            String literalValue = strValue.substring(1, strValue.length() - 1);
            StringLiteral stringLiteral = ast.newStringLiteral();
            stringLiteral.setLiteralValue(literalValue);
            return stringLiteral;
        }

        // Check if it's a qualified enum name (e.g., enum constant like TemporalType.DATE)
        if (strValue.contains(".")) {
            String[] parts = strValue.split("\\.");
            String typeName = parts[0]; // e.g., "TemporalType"
            String fieldName = parts[1]; // e.g., "DATE"

            // Infer the package from the annotation package
            String annotationPackage = annotation.substring(0, annotation.lastIndexOf('.'));
            String fullyQualifiedTypeName = annotationPackage + "." + typeName;

            // Add import for the type
            String importedTypeName = imports.addImport(fullyQualifiedTypeName, importRewriteContext);

            // Create qualified name using the imported type name
            return ast.newQualifiedName(
                                        ast.newSimpleName(importedTypeName),
                                        ast.newSimpleName(fieldName));
        }

        // Simple name
        return ast.newSimpleName(strValue);
    }
}
