/*******************************************************************************
* Copyright (c) 2024 IBM Corporation and others.
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

public class ResourcesAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void GeneratedAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/annotations/ResourcesAnnotation.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // expected annotations
        Diagnostic d1 = d(17, 14, 37, "The @Resource annotation must define the attribute 'type'.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "MissingResourceTypeAttribute");

        Diagnostic d2 = d(17, 39, 69, "The @Resource annotation must define the attribute 'name'.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "MissingResourceNameAttribute");

        Diagnostic d3 = d(21, 0, 15, "The @Resources annotation must define at least one sub-annotation '@Resource'.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "MissingResourceAnnotation");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te1 = te(17, 0, 18, 0, "@Resources({ @Resource(name = \"aaa\", type = \"\"), @Resource(type = Object.class) })\n");
        CodeAction ca1 = ca(uri, "Insert 'type' attribute to @Resource", d1, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca1);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d2);
        TextEdit te2 = te(17, 0, 18, 0, "@Resources({ @Resource(name = \"aaa\"), @Resource(type = Object.class, name = \"\") })\n");
        CodeAction ca2 = ca(uri, "Insert 'name' attribute to @Resource", d2, te2);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca2);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d3);
        TextEdit te3 = te(21, 0, 21, 15, "@Resources({ @Resource(name = \"\", type = \"\") })");
        CodeAction ca3 = ca(uri, "Insert 'name,type' attributes to @Resource", d3, te3);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3);
    }
}
