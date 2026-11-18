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

package org.eclipse.lsp4jakarta.jdt.test.cdi;

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.ca;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.test.core.BaseJakartaTest;
import org.junit.Test;

/**
 * Tests for CDI §2.2.1 legal bean type diagnostics — bare type variable at injection points.
 *
 * According to CDI specification section 2.2.1:
 * "A type variable is not a legal bean type."
 *
 * Tests cover:
 * - @Inject fields whose type is a bare type variable (T or T[])
 * - @Inject method parameters whose type is a bare type variable (T or T[])
 * - Multi-parameter @Inject methods with bare type variables
 * - Non-generic classes with method-level type variables (<U>)
 * - Non-generic classes with valid injection points (no diagnostics)
 * - Quickfix: Remove @Inject annotation from the offending element
 */
public class CdiBareTypeVariableInjectTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void bareTypeVariableAtInjectionPoints() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/BareTypeVariableInjectBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 39 (0-based 38): T bareTypeField — col 6..19
        Diagnostic injectBareTypeField = d(38, 6, 19,
                                           "A type variable is not a legal bean type. Injection point fields must not use a bare type variable (T or T[]).",
                                           DiagnosticSeverity.Error, "jakarta-cdi",
                                           "InvalidBareTypeVariableInInjectField");

        // Line 43 (0-based 42): T[] bareTypeArrayField — col 8..26
        Diagnostic injectBareTypeArrayField = d(42, 8, 26,
                                                "A type variable is not a legal bean type. Injection point fields must not use a bare type variable (T or T[]).",
                                                DiagnosticSeverity.Error, "jakarta-cdi",
                                                "InvalidBareTypeVariableInInjectField");

        // Line 47 (0-based 46): setBareType — method name col 16..27 (diagnostic on method)
        Diagnostic injectBareTypeMethodParam = d(46, 16, 27,
                                                 "A type variable is not a legal bean type. Parameter 'value' must not use a bare type variable (T or T[]).",
                                                 DiagnosticSeverity.Error, "jakarta-cdi",
                                                 "InvalidBareTypeVariableInInjectMethodParam");

        // Line 52 (0-based 51): setBareTypeArray — method name col 16..32 (diagnostic on method)
        Diagnostic injectBareTypeArrayMethodParam = d(51, 16, 32,
                                                      "A type variable is not a legal bean type. Parameter 'values' must not use a bare type variable (T or T[]).",
                                                      DiagnosticSeverity.Error, "jakarta-cdi",
                                                      "InvalidBareTypeVariableInInjectMethodParam");

        // Line 57 (0-based 56): setMultipleParams — method name col 16..33 (bareParam)
        Diagnostic injectBareTypeMultiParam = d(56, 16, 33,
                                                "A type variable is not a legal bean type. Parameter 'bareParam' must not use a bare type variable (T or T[]).",
                                                DiagnosticSeverity.Error, "jakarta-cdi",
                                                "InvalidBareTypeVariableInInjectMethodParam");

        // Line 57 (0-based 56): setMultipleParams — method name col 16..33 (bareArrayParam)
        Diagnostic injectBareTypeMultiArrayParam = d(56, 16, 33,
                                                     "A type variable is not a legal bean type. Parameter 'bareArrayParam' must not use a bare type variable (T or T[]).",
                                                     DiagnosticSeverity.Error, "jakarta-cdi",
                                                     "InvalidBareTypeVariableInInjectMethodParam");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              injectBareTypeField,
                              injectBareTypeArrayField,
                              injectBareTypeMethodParam,
                              injectBareTypeArrayMethodParam,
                              injectBareTypeMultiParam,
                              injectBareTypeMultiArrayParam);

        // --- QuickFix: Remove @Inject from T bareTypeField (line 38, 0-based 37) ---
        JakartaJavaCodeActionParams codeActionBareTypeField = createCodeActionParams(uri, injectBareTypeField);
        TextEdit removeBareTypeFieldInject = te(37, 4, 38, 4, "");
        CodeAction removeBareTypeFieldInjectAction = ca(uri, "Remove @Inject", injectBareTypeField,
                                                        removeBareTypeFieldInject);
        assertJavaCodeAction(codeActionBareTypeField, IJDT_UTILS, removeBareTypeFieldInjectAction);

        // --- QuickFix: Remove @Inject from T[] bareTypeArrayField (line 42, 0-based 41) ---
        JakartaJavaCodeActionParams codeActionBareTypeArrayField = createCodeActionParams(uri, injectBareTypeArrayField);
        TextEdit removeBareTypeArrayFieldInject = te(41, 4, 42, 4, "");
        CodeAction removeBareTypeArrayFieldInjectAction = ca(uri, "Remove @Inject", injectBareTypeArrayField,
                                                             removeBareTypeArrayFieldInject);
        assertJavaCodeAction(codeActionBareTypeArrayField, IJDT_UTILS, removeBareTypeArrayFieldInjectAction);

        // --- QuickFix: Remove @Inject from setBareType method (@Inject on line 45, 0-based) ---
        JakartaJavaCodeActionParams codeActionBareTypeMethodParam = createCodeActionParams(uri,
                                                                                           injectBareTypeMethodParam);
        TextEdit removeBareTypeMethodInject = te(45, 4, 46, 4, "");
        CodeAction removeBareTypeMethodInjectAction = ca(uri, "Remove @Inject", injectBareTypeMethodParam,
                                                         removeBareTypeMethodInject);
        assertJavaCodeAction(codeActionBareTypeMethodParam, IJDT_UTILS, removeBareTypeMethodInjectAction);

        // --- QuickFix: Remove @Inject from setBareTypeArray method (@Inject on line 50, 0-based) ---
        JakartaJavaCodeActionParams codeActionBareTypeArrayMethodParam = createCodeActionParams(uri,
                                                                                                injectBareTypeArrayMethodParam);
        TextEdit removeBareTypeArrayMethodInject = te(50, 4, 51, 4, "");
        CodeAction removeBareTypeArrayMethodInjectAction = ca(uri, "Remove @Inject", injectBareTypeArrayMethodParam,
                                                              removeBareTypeArrayMethodInject);
        assertJavaCodeAction(codeActionBareTypeArrayMethodParam, IJDT_UTILS, removeBareTypeArrayMethodInjectAction);

        // --- QuickFix: Remove @Inject from setMultipleParams method (@Inject on line 55, 0-based) ---
        JakartaJavaCodeActionParams codeActionMultiParam = createCodeActionParams(uri, injectBareTypeMultiParam);
        TextEdit removeMultiParamMethodInject = te(55, 4, 56, 4, "");
        CodeAction removeMultiParamMethodInjectAction = ca(uri, "Remove @Inject", injectBareTypeMultiParam,
                                                           removeMultiParamMethodInject);
        assertJavaCodeAction(codeActionMultiParam, IJDT_UTILS, removeMultiParamMethodInjectAction);
    }

    @Test
    public void validNonGenericInjectionPoints() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ValidBareTypeVariableInjectBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected: all @Inject fields and method parameters use concrete types.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void bareTypeVariableAtNonGenericInjectMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/NonGenericBareTypeInjectBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 23 (0-based 22): setMethodLevelBareType — method name col 20..42
        Diagnostic injectMethodLevelBareType = d(22, 20, 42,
                                                 "A type variable is not a legal bean type. Parameter 'value' must not use a bare type variable (T or T[]).",
                                                 DiagnosticSeverity.Error, "jakarta-cdi",
                                                 "InvalidBareTypeVariableInInjectMethodParam");
        Diagnostic injectMethodLevelGeneric = d(22, 20, 42,
                                                "The @Inject annotation must not define a generic method.",
                                                DiagnosticSeverity.Error, "jakarta-di",
                                                "InvalidInjectAnnotationOnGenericMethod");

        // Line 28 (0-based 27): setMethodLevelBareTypeArray — method name col 20..47
        Diagnostic injectMethodLevelBareTypeArray = d(27, 20, 47,
                                                      "A type variable is not a legal bean type. Parameter 'values' must not use a bare type variable (T or T[]).",
                                                      DiagnosticSeverity.Error, "jakarta-cdi",
                                                      "InvalidBareTypeVariableInInjectMethodParam");
        Diagnostic injectMethodLevelArrayGeneric = d(27, 20, 47,
                                                     "The @Inject annotation must not define a generic method.",
                                                     DiagnosticSeverity.Error, "jakarta-di",
                                                     "InvalidInjectAnnotationOnGenericMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              injectMethodLevelArrayGeneric,
                              injectMethodLevelBareTypeArray,
                              injectMethodLevelGeneric,
                              injectMethodLevelBareType);

        // --- QuickFix: Remove @Inject from setMethodLevelBareType method (@Inject on line 21, 0-based) ---
        JakartaJavaCodeActionParams codeActionMethodLevelBare = createCodeActionParams(uri,
                                                                                       injectMethodLevelBareType);
        TextEdit removeMethodLevelBareEdit = te(21, 4, 22, 4, "");
        CodeAction removeMethodLevelBareAction = ca(uri, "Remove @Inject", injectMethodLevelBareType,
                                                    removeMethodLevelBareEdit);
        assertJavaCodeAction(codeActionMethodLevelBare, IJDT_UTILS, removeMethodLevelBareAction);

        // --- QuickFix: Remove @Inject from setMethodLevelBareTypeArray method (@Inject on line 26, 0-based) ---
        JakartaJavaCodeActionParams codeActionMethodLevelBareArray = createCodeActionParams(uri,
                                                                                            injectMethodLevelBareTypeArray);
        TextEdit removeMethodLevelBareArrayEdit = te(26, 4, 27, 4, "");
        CodeAction removeMethodLevelBareArrayAction = ca(uri, "Remove @Inject", injectMethodLevelBareTypeArray,
                                                         removeMethodLevelBareArrayEdit);
        assertJavaCodeAction(codeActionMethodLevelBareArray, IJDT_UTILS, removeMethodLevelBareArrayAction);
    }
}
