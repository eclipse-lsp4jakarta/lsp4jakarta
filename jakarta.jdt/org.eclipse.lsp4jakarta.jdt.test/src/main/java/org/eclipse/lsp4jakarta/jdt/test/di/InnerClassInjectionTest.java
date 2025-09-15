/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.jdt.test.di;

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

public class InnerClassInjectionTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void NonStaticInnerClassInjection() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/InnerClassInjection.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Create expected diagnostics.
        Diagnostic d1 = d(22, 19, 23, "Cannot inject non-static inner class. Injection target must be a top-level or static nested class.",
                          DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnNonStaticInnerClass");
        // d1.setData(IType.FIELD);

        Diagnostic d2 = d(29, 13, 20, "Cannot inject non-static inner class. Injection target must be a top-level or static nested class.",
                          DiagnosticSeverity.Error, "jakarta-di", "InvalidInjectAnnotationOnNonStaticInnerClass");
        // d2.setData(IType.METHOD);

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        // Create expected quick fixes.
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te = te(33, 7, 33, 7, " static");
        CodeAction ca = ca(uri, "Add 'static' modifier to the nested class", d1, te);
        TextEdit te1 = te(21, 1, 22, 1, "");
        CodeAction ca1 = ca(uri, "Remove @Inject", d1, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca, ca1);

        codeActionParams = createCodeActionParams(uri, d2);
        te = te(33, 7, 33, 7, " static");
        ca = ca(uri, "Add 'static' modifier to the nested class", d2, te);
        te1 = te(28, 1, 29, 1, "");
        ca1 = ca(uri, "Remove @Inject", d2, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca, ca1);
    }
}
