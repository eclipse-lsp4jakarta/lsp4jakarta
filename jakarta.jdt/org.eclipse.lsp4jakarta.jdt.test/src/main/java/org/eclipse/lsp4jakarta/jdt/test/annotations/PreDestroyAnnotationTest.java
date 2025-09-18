/*******************************************************************************
* Copyright (c) 2021, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.test.annotations;

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

import com.google.gson.Gson;

public class PreDestroyAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void GeneratedAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/annotations/PreDestroyAnnotation.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // expected annotations

        Diagnostic d1 = d(24, 16, 28, "A method with the @PreDestroy annotation must not have any parameters.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PreDestroyParams");

        Diagnostic d2 = d(31, 20, 31, "A method with the @PreDestroy annotation must not be static.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PreDestroyStatic");

        Diagnostic d3 = d(36, 13, 25, "A method with the annotation '@PreDestroy' must not throw checked exceptions.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PreDestroyException");
        d3.setData(new Gson().toJsonTree(Arrays.asList("java.lang.Exception")));

        Diagnostic d4 = d(51, 13, 29, "A method with the annotation '@PreDestroy' must not throw checked exceptions.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PreDestroyException");
        d4.setData(new Gson().toJsonTree(Arrays.asList("java.io.IOException")));

        Diagnostic d5 = d(56, 13, 34, "A method with the annotation '@PreDestroy' must not throw checked exceptions.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PreDestroyException");
        d5.setData(new Gson().toJsonTree(Arrays.asList("io.openliberty.sample.jakarta.annotations.CustomCheckedException")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d2, d1, d3, d4, d5);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te = te(23, 1, 24, 1, "");
        TextEdit te1 = te(24, 29, 24, 40, "");
        CodeAction ca = ca(uri, "Remove @PreDestroy", d1, te);
        CodeAction ca1 = ca(uri, "Remove all parameters", d1, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca, ca1);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d2);
        TextEdit te2 = te(30, 1, 31, 1, "");
        TextEdit te3 = te(31, 7, 31, 14, "");
        CodeAction ca2 = ca(uri, "Remove @PreDestroy", d2, te2);
        CodeAction ca3 = ca(uri, "Remove the 'static' modifier", d2, te3);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca2, ca3);

    }

}
