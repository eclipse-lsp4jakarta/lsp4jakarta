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
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.jsonp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4jakarta.jdt.core.ASTUtils;

/**
 * Detects JSON-P code that reads a JsonObject and copies its values into a plain
 * Java object field by field, which Jakarta JSON Binding (Jsonb.fromJson) can do
 * with less code and type safety.
 *
 * <p>Within a single method, a {@code JsonReader.readObject()} call is reported when at least
 * {@link Constants#MIN_MANUAL_MAPPINGS} values read from the resulting JsonObject are passed to a
 * setter or constructor, or assigned to a field, of a non-JSON object. Values that are only
 * inspected, or generic JSON-P processing, are not reported.
 *
 * <p>https://openliberty.io/docs/latest/json-p-b.html
 */
public class ManualJsonMappingAnalyzer {

    private ManualJsonMappingAnalyzer() {}

    /**
     * Finds the {@code JsonReader.readObject()} invocations whose result is mapped manually into an object.
     *
     * @param allMethodInvocations all method invocations of the compilation unit
     * @return the readObject() invocations to report, in source order
     */
    public static List<MethodInvocation> findManualMappings(List<MethodInvocation> allMethodInvocations) {
        List<MethodInvocation> result = new ArrayList<>();
        // Most files have no JsonReader.readObject() call, so exit before any further work
        List<MethodInvocation> readObjectInvocations = allMethodInvocations.stream().filter(ManualJsonMappingAnalyzer::isJsonReaderReadObject).collect(Collectors.toList());
        if (readObjectInvocations.isEmpty()) {
            return result;
        }
        List<MethodInvocation> mappedGetters = allMethodInvocations.stream().filter(ManualJsonMappingAnalyzer::isJsonObjectValueGetter).filter(ManualJsonMappingAnalyzer::isCopiedIntoObject).collect(Collectors.toList());
        if (mappedGetters.size() < Constants.MIN_MANUAL_MAPPINGS) {
            return result;
        }
        for (MethodInvocation readObject : readObjectInvocations) {
            MethodDeclaration enclosingMethod = ASTUtils.getEnclosingMethod(readObject);
            if (enclosingMethod == null) {
                continue;
            }
            IVariableBinding assignedVariable = getAssignedVariable(readObject);
            long mappedCount = mappedGetters.stream().filter(getter -> ASTUtils.getEnclosingMethod(getter) == enclosingMethod
                                                                       && isReadFrom(getter, readObject, assignedVariable)).count();
            if (mappedCount >= Constants.MIN_MANUAL_MAPPINGS) {
                result.add(readObject);
            }
        }
        return result;
    }

    private static boolean isJsonReaderReadObject(MethodInvocation methodInvocation) {
        return Constants.JSON_READER_READ_OBJECT.equals(methodInvocation.getName().getIdentifier())
               && ASTUtils.isMatchedTargetClass(methodInvocation, Constants.JAKARTA_JSON_READER_FQ_NAME);
    }

    /** Any JsonObject method that reads a value, e.g. getString, getInt, getJsonNumber. */
    private static boolean isJsonObjectValueGetter(MethodInvocation methodInvocation) {
        return methodInvocation.getName().getIdentifier().startsWith(Constants.GETTER_PREFIX)
               && ASTUtils.isMatchedTargetClass(methodInvocation, Constants.JAKARTA_JSON_OBJECT_FQ_NAME);
    }

    /**
     * Checks that the getter is called on the JsonObject returned by the given readObject() invocation,
     * either directly (chained call) or through the variable it was assigned to.
     *
     * @param assignedVariable the variable readObject() is assigned to, or null if it is not assigned
     */
    private static boolean isReadFrom(MethodInvocation getter, MethodInvocation readObject, IVariableBinding assignedVariable) {
        Expression receiver = getter.getExpression();
        while (receiver instanceof ParenthesizedExpression parenthesized) {
            receiver = parenthesized.getExpression();
        }
        if (receiver == readObject) {
            return true;
        }
        if (receiver instanceof Name name) {
            return assignedVariable != null && assignedVariable.isEqualTo(name.resolveBinding());
        }
        return false;
    }

    /**
     * Returns the variable the result of the given invocation is stored in, or null.
     */
    private static IVariableBinding getAssignedVariable(MethodInvocation invocation) {
        ASTNode parent = invocation.getParent();
        if (parent instanceof VariableDeclarationFragment fragment && fragment.getInitializer() == invocation) {
            return fragment.resolveBinding();
        }
        if (parent instanceof Assignment assignment && assignment.getRightHandSide() == invocation
            && assignment.getLeftHandSide() instanceof Name name
            && name.resolveBinding() instanceof IVariableBinding variable) {
            return variable;
        }
        return null;
    }

    /** True when the invocation returns a jakarta.json type such as JsonNumber or JsonObject. */
    private static boolean returnsJsonType(MethodInvocation invocation) {
        ITypeBinding returnType = invocation.resolveTypeBinding();
        return returnType != null && returnType.getQualifiedName().startsWith(Constants.JAKARTA_JSON_PACKAGE_PREFIX);
    }

    /** A setter name is "set" followed by an upper-case letter, e.g. setName. */
    private static boolean isSetterName(String name) {
        return name.length() > Constants.SETTER_PREFIX.length() && name.startsWith(Constants.SETTER_PREFIX)
               && Character.isUpperCase(name.charAt(Constants.SETTER_PREFIX.length()));
    }

    /**
     * Checks that the value returned by the getter is stored into a non-JSON object, which means
     * it is passed to a setter or constructor, or assigned to a field. One call on the returned
     * value is allowed, e.g. {@code json.getJsonNumber("x").doubleValue()}. A getter that returns
     * a JSON type (JsonNumber, JsonObject, ...) counts only when such a call turns it into a plain value.
     */
    private static boolean isCopiedIntoObject(MethodInvocation getter) {
        Expression value = getter;
        if (getter.getParent() instanceof MethodInvocation chained && chained.getExpression() == getter) {
            value = chained;
        }
        if (value == getter && returnsJsonType(getter)) {
            return false;
        }
        ASTNode parent = value.getParent();
        if (parent instanceof MethodInvocation setter && setter.arguments().contains(value)) {
            String declaringClass = ASTUtils.getDeclaringClassName(setter);
            return isSetterName(setter.getName().getIdentifier()) && setter.arguments().size() == 1
                   && declaringClass != null && !declaringClass.startsWith(Constants.JAKARTA_JSON_PACKAGE_PREFIX);
        }
        if (parent instanceof ClassInstanceCreation creation && creation.arguments().contains(value)) {
            ITypeBinding type = creation.resolveTypeBinding();
            if (type == null) {
                return false;
            }
            String typeName = type.getQualifiedName();
            // JDK types (File, BigDecimal, ...) are value conversions, not POJO mapping
            return !typeName.startsWith(Constants.JAKARTA_JSON_PACKAGE_PREFIX) && !typeName.startsWith("java.") && !typeName.startsWith("javax.");
        }
        if (parent instanceof Assignment assignment && assignment.getRightHandSide() == value) {
            Expression target = assignment.getLeftHandSide();
            IBinding binding = null;
            if (target instanceof FieldAccess fieldAccess) {
                binding = fieldAccess.resolveFieldBinding();
            } else if (target instanceof Name name) {
                binding = name.resolveBinding();
            }
            return binding instanceof IVariableBinding variable && variable.isField();
        }
        return false;
    }
}
