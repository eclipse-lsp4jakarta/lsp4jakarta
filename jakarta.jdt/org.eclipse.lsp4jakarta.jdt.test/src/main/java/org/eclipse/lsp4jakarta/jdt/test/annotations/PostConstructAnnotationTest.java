/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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

import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.ca;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.d;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.te;

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
import org.eclipse.lsp4jakarta.jdt.core.BaseJakartaTest;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.junit.Test;

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

        Diagnostic d1 = d(15, 19, 31, "A method with the @PostConstruct annotation must be void.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PostConstructReturnType");

        Diagnostic d2 = d(20, 16, 28, "A method with the @PostConstruct annotation must not have any parameters.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "PostConstructParams");

        Diagnostic d3 = d(25, 16, 28, "A method with the @PostConstruct annotation must not throw checked exceptions.",
                          DiagnosticSeverity.Warning, "jakarta-annotations", "PostConstructException");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d2);
        TextEdit te1 = te(19, 4, 20, 4, "");
        TextEdit te2 = te(20, 29, 20, 40, "");
        CodeAction ca1 = ca(uri, "Remove @PostConstruct", d2, te1);
        CodeAction ca2 = ca(uri, "Remove all parameters", d2, te2);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1, ca2);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d1);
        TextEdit te3 = te(15, 11, 15, 18, "void");
        CodeAction ca3 = ca(uri, "Change return type to void", d1, te3);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3);
    }

}
