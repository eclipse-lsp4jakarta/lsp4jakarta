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
 * - Quickfix: Remove @Inject annotation from the offending element
 */
public class CdiBareTypeVariableInjectTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void bareTypeVariableAtInjectField() throws Exception {
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

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              injectBareTypeField,
                              injectBareTypeArrayField,
                              injectBareTypeMethodParam,
                              injectBareTypeArrayMethodParam);

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
    }
}
