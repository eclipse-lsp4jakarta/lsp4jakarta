/*******************************************************************************
* Copyright (c) 2021, 2026 IBM Corporation and others.
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

public class GeneratedAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void GeneratedAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/annotations/GeneratedAnnotation.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // expected annotations
        Diagnostic d1 = d(4, 0, 31,
                          "The @Generated annotation attribute 'value' must have the name of the code generator.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "GeneratedValueEmpty");
        Diagnostic d2 = d(7, 4, 67,
                          "The @Generated annotation must define the attribute 'date' following the ISO 8601 standard.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "InvalidDateFormat");
        Diagnostic d3 = d(10, 4, 74,
                          "The @Generated annotation attribute 'value' should use the fully qualified name of the code generator.",
                          DiagnosticSeverity.Warning, "jakarta-annotations", "GeneratedValueInvalidFormat");

        Diagnostic d4 = d(13, 4, 66,
                          "The @Generated annotation must define the attribute 'date' following the ISO 8601 standard.",
                          DiagnosticSeverity.Error, "jakarta-annotations", "InvalidDateFormat");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4);
    }
}