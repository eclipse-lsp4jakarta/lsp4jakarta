/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation, Matthew Shocrylas, Bera Sogut and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation, Matthew Shocrylas - initial API and implementation, Bera Sogut
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.jaxrs;

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

public class ResourceMethodTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void NonPublicMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/jaxrs/NotPublicResourceMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(20, 17, 30, "Only public methods can be exposed as resource methods.",
                         DiagnosticSeverity.Error, "jakarta-jaxrs", "NonPublicResourceMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);

        // Test for quick-fix code action
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
        TextEdit te = te(20, 4, 20, 11, "public"); // range may need to change
        CodeAction ca = ca(uri, "Make method public", d, te);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca);
    }

    @Test
    public void multipleEntityParamsMethod() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/jaxrs/MultipleEntityParamsResourceMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(24, 13, 46, "Resource methods cannot have more than one entity parameter.",
                          DiagnosticSeverity.Error, "jakarta-jaxrs", "ResourceMethodMultipleEntityParams");
        Diagnostic d2 = d(36, 13, 55, "Resource methods cannot have more than one entity parameter.",
                          DiagnosticSeverity.Error, "jakarta-jaxrs", "ResourceMethodMultipleEntityParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        // Test for quick-fix code action
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);

        TextEdit te1 = te(21, 112, 21, 130, "");
        CodeAction ca1 = ca(uri, "Remove all entity parameters except entityParam1", d1, te1);

        TextEdit te2 = te(21, 47, 21, 68, "");
        CodeAction ca2 = ca(uri, "Remove all entity parameters except entityParam2", d1, te2);
        System.out.println("hey man");
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1, ca2);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);

        TextEdit te3 = te(21, 112, 21, 130, "");
        CodeAction ca3 = ca(uri, "Remove all entity parameters except entityString", d1, te1);

        TextEdit te4 = te(21, 47, 21, 68, "");
        CodeAction ca4 = ca(uri, "Remove all entity parameters except entityInt", d1, te2);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3, ca4);
    }

}
