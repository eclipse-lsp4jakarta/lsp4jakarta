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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
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
                try {
                    String msg = Messages.getMessage("CreatePointerErrorMessage");
                    Range range = JDTUtils.toRange(unit, arg.getStartPosition(), arg.getLength());
                    diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                             ErrorCode.InvalidJsonCreatePointerTarget, DiagnosticSeverity.Error));
                } catch (JavaModelException e) {
                    JakartaCorePlugin.logException("Cannot calculate diagnostics", e);
                }
            }
        }

        //Used to get the list of method invocations for JsonObjectBuilder add methods
        List<MethodInvocation> createObjectBuilderMethodInvocations = allMethodInvocations.stream().filter(mi -> {
            try {
                return isMatchedJsonObjectBuilder(unit, mi);
            } catch (JavaModelException e) {
                return false;
            }
        }).collect(Collectors.toList());
        for (MethodInvocation m : createObjectBuilderMethodInvocations) {
            if (!m.arguments().isEmpty()) {
                Expression arg = (Expression) m.arguments().get(0);
                if (arg instanceof NullLiteral) {
                    //https://jakarta.ee/specifications/jsonp/2.1/apidocs/jakarta.json/jakarta/json/jsonobjectbuilder
                    //Does not allow key to be null for JsonObjectBuilder.add() method
                    try {
                        Range range = JDTUtils.toRange(unit, arg.getStartPosition(), arg.getLength());
                        diagnostics.add(context.createDiagnostic(uri, Messages.getMessage("ErrorMessageJsonPObjectKeyNonNull"),
                                                                 range, Constants.DIAGNOSTIC_SOURCE, ErrorCode.InvalidJsonObjectBuilderKey, DiagnosticSeverity.Error));
                    } catch (JavaModelException e) {
                        LOGGER.log(Level.SEVERE, "Cannot calculate diagnostics", e.getMessage());
                    }
                }
            }
        }
        return diagnostics;
    }

    /**
     * Method used to identify jakarta.json.JsonObjectBuilder.add type method invocations
     *
     * @param unit
     * @param mi
     * @return boolean
     * @throws JavaModelException
     */
    private boolean isMatchedJsonObjectBuilder(ICompilationUnit unit, MethodInvocation mi) throws JavaModelException {
        IMethodBinding binding = mi.resolveMethodBinding();
        if (binding != null) {
            ITypeBinding declaringClass = binding.getDeclaringClass();
            if (declaringClass != null) {
                String fqName = declaringClass.getQualifiedName() + "." + binding.getName();
                if (Constants.JAKARTA_JSON_OBJECT_BUILDER_ADD.equals(fqName)) {
                    return true;
                }
            }
        }
        return false;
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
