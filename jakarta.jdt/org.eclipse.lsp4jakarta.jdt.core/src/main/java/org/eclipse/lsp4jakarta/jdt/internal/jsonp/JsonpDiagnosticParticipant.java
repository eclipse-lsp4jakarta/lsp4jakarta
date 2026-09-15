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
package org.eclipse.lsp4jakarta.jdt.internal.jsonp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.ASTUtils;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaErrorCode;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Json Processing (JSON-P) diagnostic participant.
 */
public class JsonpDiagnosticParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(JsonpDiagnosticParticipant.class.getName());

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        List<MethodInvocation> allMethodInvocations = ASTUtils.getMethodInvocations(unit);
        List<MethodInvocation> createPointerInvocations = allMethodInvocations.stream().filter(mi -> {
            try {
                return isMatchedJsonCreatePointer(unit, mi);
            } catch (JavaModelException e) {
                return false;
            }
        }).collect(Collectors.toList());
        for (MethodInvocation m : createPointerInvocations) {
            Expression arg = (Expression) m.arguments().get(0);
            if (isInvalidArgument(arg)) {
                // If the argument supplied to a createPointer invocation is a String literal
                // and is neither an empty String
                // or a sequence of '/' prefixed tokens, a diagnostic highlighting the invalid
                // argument is created.
                String msg = Messages.getMessage("CreatePointerErrorMessage");
                createDiagnostic(unit, diagnostics, context, uri, msg, ErrorCode.InvalidJsonCreatePointerTarget, arg);
            }
        }

        // Single pass to collect both JsonObjectBuilder and JsonArrayBuilder method invocations
        Map<JSONBuilderType, List<MethodInvocation>> builderInvocations = allMethodInvocations.stream().collect(Collectors.groupingBy(mi -> {
            try {
                return getJsonBuilderType(unit, mi);
            } catch (JavaModelException e) {
                return JSONBuilderType.UNKNOWN;
            }
        }));

        List<MethodInvocation> createObjectBuilderMethodInvocations = builderInvocations.getOrDefault(JSONBuilderType.OBJECT, Collections.emptyList());
        List<MethodInvocation> createArrayBuilderMethodInvocations = builderInvocations.getOrDefault(JSONBuilderType.ARRAY, Collections.emptyList());
        //Used to create diagnostics for invalid JsonObjectBuilder add methods
        createDiagnosticsForBuilderInvocations(unit, createObjectBuilderMethodInvocations, diagnostics, context, uri,
                                               Messages.getMessage("ErrorMessageJsonPObjectKeyNonNull"),
                                               ErrorCode.InvalidJsonObjectBuilderKey);
        //Used to create diagnostics for invalid JsonArrayBuilder add methods
        createDiagnosticsForBuilderInvocations(unit, createArrayBuilderMethodInvocations, diagnostics, context, uri,
                                               Messages.getMessage("ErrorMessageJsonPArrayValueNonNull"),
                                               ErrorCode.InvalidJsonArrayBuilderValue);
        return diagnostics;
    }

    /**
     * Method used to create diagnostics for invalid JsonObjectBuilder or JsonArrayBuilder
     *
     * https://jakarta.ee/specifications/jsonp/2.1/apidocs/jakarta.json/jakarta/json/jsonobjectbuilder
     * Does not allow key to be null for JsonObjectBuilder.add() method
     *
     * https://jakarta.ee/specifications/jsonp/2.1/apidocs/jakarta.json/jakarta/json/jsonarraybuilder
     * Does not allow value to be null for JsonArrayBuilder.add() method
     *
     * @param unit
     * @param invocations
     * @param diagnostics
     * @param context
     * @param uri
     * @param msg
     * @param errCode
     */
    private void createDiagnosticsForBuilderInvocations(ICompilationUnit unit, List<MethodInvocation> invocations,
                                                        List<Diagnostic> diagnostics, JavaDiagnosticsContext context,
                                                        String uri, String msg, IJavaErrorCode errCode) {
        for (MethodInvocation methodIn : invocations) {
            if (!methodIn.arguments().isEmpty()) {
                for (Object argObj : methodIn.arguments()) {
                    Expression arg = (Expression) argObj;
                    if (isInvalidNullArgument(arg)) {
                        createDiagnostic(unit, diagnostics, context, uri, msg, errCode, arg);
                    }
                }
            }
        }
    }

    /**
     * Common method to create a diagnostic for an invalid argument
     *
     * @param unit
     * @param diagnostics
     * @param context
     * @param uri
     * @param msg
     * @param errCode
     * @param arg
     */
    private void createDiagnostic(ICompilationUnit unit, List<Diagnostic> diagnostics, JavaDiagnosticsContext context,
                                  String uri, String msg, IJavaErrorCode errCode, Expression arg) {
        try {
            Range range = JDTUtils.toRange(unit, arg.getStartPosition(), arg.getLength());
            diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE, errCode,
                                                     DiagnosticSeverity.Error));
        } catch (JavaModelException e) {
            LOGGER.log(Level.SEVERE, "Cannot calculate diagnostics", e.getMessage());
        }
    }

    /**
     * Method is used to check if value of arg passed or Cast Expression inside passed arg is null
     *
     * @param arg
     * @return boolean
     */
    private boolean isInvalidNullArgument(Expression arg) {
        return arg instanceof NullLiteral
               || (arg instanceof CastExpression cast && cast.getExpression() instanceof NullLiteral);
    }

    /**
     * Method used to identify jakarta.json.JsonObjectBuilder.add or jakarta.json.JsonArrayBuilder.add type method invocations
     *
     * @param unit
     * @param mi
     * @return enum JSONBuilderType
     * @throws JavaModelException
     */
    private JSONBuilderType getJsonBuilderType(ICompilationUnit unit, MethodInvocation mi) throws JavaModelException {
        IMethodBinding binding = mi.resolveMethodBinding();
        if (!Constants.JAKARTA_JSON_BUILDER_ADD_METHOD.equals(mi.getName().getIdentifier())
            || binding == null) {
            return JSONBuilderType.UNKNOWN;
        }
        ITypeBinding methodTargetClass = binding.getDeclaringClass();
        String qualifiedName = (methodTargetClass != null) ? methodTargetClass.getQualifiedName() : null;
        if (Constants.JAKARTA_JSON_OBJECT_BUILDER_FQ_NAME.equals(qualifiedName)) {
            return JSONBuilderType.OBJECT;
        }
        if (Constants.JAKARTA_JSON_ARRAY_BUILDER_FQ_NAME.equals(qualifiedName)) {
            return JSONBuilderType.ARRAY;
        }
        return JSONBuilderType.UNKNOWN;
    }

    private boolean isInvalidArgument(Expression arg) {
        if (arg instanceof StringLiteral) {
            String argValue = ((StringLiteral) arg).getLiteralValue();
            if (!(argValue.isEmpty() || argValue.matches("^(\\/[^\\/]+)+$"))) {
                return true;
            }
        }

        return false;
    }

    private boolean isMatchedJsonCreatePointer(ICompilationUnit unit, MethodInvocation mi) throws JavaModelException {
        if (mi.arguments().size() == 1 && Constants.CREATE_POINTER.equals(mi.getName().getIdentifier())
            && mi.getExpression() != null) {
            Expression ex = mi.getExpression();
            String qualifier = ex.toString();
            if (Constants.JSON_FQ_NAME.endsWith(qualifier)) {
                // For performance reason, we check if the import of Java element name is
                // declared
                if (DiagnosticUtils.isImportedJavaElement(unit, Constants.JSON_FQ_NAME) == true)
                    return true;
                // only check fully qualified java element
                if (Constants.JSON_FQ_NAME.equals(qualifier)) {
                    ITypeBinding itb = ex.resolveTypeBinding();
                    return itb != null && qualifier.equals(itb.getQualifiedName());
                }
            }
        }

        return false;
    }
}
