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

public class PostConstructAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void GeneratedAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/annotations/PostConstructAnnotation.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // expected Diagnostics

        Diagnostic d1 = d(19, 16, 28, "A method with the @PostConstruct annotation must be void.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PostConstructReturnType");

        Diagnostic d2 = d(24, 13, 25, "A method with the @PostConstruct annotation must not have any parameters.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PostConstructParams");

        Diagnostic d3 = d(29, 13, 25, "A method with the annotation '@PostConstruct' must not throw checked exceptions.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PostConstructException");
        d3.setData(new Gson().toJsonTree(Arrays.asList("java.lang.Exception")));

        Diagnostic d4 = d(44, 13, 29, "A method with the annotation '@PostConstruct' must not throw checked exceptions.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PostConstructException");
        d4.setData(new Gson().toJsonTree(Arrays.asList("java.io.IOException")));

        Diagnostic d5 = d(49, 13, 28, "A method with the annotation '@PostConstruct' must not throw checked exceptions.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PostConstructException");
        d5.setData(new Gson().toJsonTree(Arrays.asList("io.openliberty.sample.jakarta.annotations.CustomCheckedException", "java.io.IOException")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d2);
        TextEdit te1 = te(23, 1, 24, 1, "");
        TextEdit te2 = te(24, 26, 24, 37, "");
        CodeAction ca1 = ca(uri, "Remove @PostConstruct", d2, te1);
        CodeAction ca2 = ca(uri, "Remove all parameters", d2, te2);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1, ca2);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d1);
        TextEdit te3 = te(19, 8, 19, 15, "void");
        CodeAction ca3 = ca(uri, "Change return type to void", d1, te3);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3);

        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, d5);
        TextEdit te51 = te(48, 1, 49, 1, "");
        TextEdit te52 = te(49, 38, 49, 99, "CustomUncheckedException");
        CodeAction ca51 = ca(uri, "Remove @PostConstruct", d5, te51);
        CodeAction ca52 = ca(uri, "Remove all checked exceptions.", d5, te52);
        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, ca51, ca52);
    }

}
