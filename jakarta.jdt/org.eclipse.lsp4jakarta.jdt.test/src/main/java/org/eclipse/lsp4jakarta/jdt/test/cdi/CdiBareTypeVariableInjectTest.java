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

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
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
 * - Valid cases that must produce no diagnostics
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

        // Line 39: T bareTypeField  — field name "bareTypeField" at col 6..19
        Diagnostic injectBareTypeField = d(38, 6, 19,
                                           "A type variable is not a legal bean type. Injection point fields must not use a bare type variable (T or T[]).",
                                           DiagnosticSeverity.Error, "jakarta-cdi",
                                           "InvalidBareTypeVariableInInjectField");

        // Line 43: T[] bareTypeArrayField  — field name "bareTypeArrayField" at col 8..26
        Diagnostic injectBareTypeArrayField = d(42, 8, 26,
                                                "A type variable is not a legal bean type. Injection point fields must not use a bare type variable (T or T[]).",
                                                DiagnosticSeverity.Error, "jakarta-cdi",
                                                "InvalidBareTypeVariableInInjectField");

        // Line 47: setBareType(T value)  — param name "value" at col 30..35
        Diagnostic injectBareTypeMethodParam = d(46, 30, 35,
                                                 "A type variable is not a legal bean type. Injection method parameters must not use a bare type variable (T or T[]).",
                                                 DiagnosticSeverity.Error, "jakarta-cdi",
                                                 "InvalidBareTypeVariableInInjectMethodParam");

        // Line 52: setBareTypeArray(T[] values)  — param name "values" at col 37..43
        Diagnostic injectBareTypeArrayMethodParam = d(51, 37, 43,
                                                      "A type variable is not a legal bean type. Injection method parameters must not use a bare type variable (T or T[]).",
                                                      DiagnosticSeverity.Error, "jakarta-cdi",
                                                      "InvalidBareTypeVariableInInjectMethodParam");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              injectBareTypeField,
                              injectBareTypeArrayField,
                              injectBareTypeMethodParam,
                              injectBareTypeArrayMethodParam);
    }
}
