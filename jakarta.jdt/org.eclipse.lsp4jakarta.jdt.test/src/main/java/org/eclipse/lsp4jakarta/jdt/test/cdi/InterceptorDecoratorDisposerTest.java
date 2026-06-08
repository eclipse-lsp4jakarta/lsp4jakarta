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
 * Tests for CDI diagnostics and quickfixes related to @Disposes in interceptors and decorators.
 */
public class InterceptorDecoratorDisposerTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void interceptorWithDisposer() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/InterceptorWithDisposer.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(15, 16, 23,
                          "Interceptors and Decorators cannot have methods with parameters annotated with @Disposes.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithDisposerMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);

        // Test quickfix
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te1 = te(15, 24, 15, 34, "");
        CodeAction ca1 = ca(uri, "Remove the '@Disposes' modifier from parameter 'conn'", d1, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca1);
    }

    @Test
    public void decoratorWithDisposer() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/DecoratorWithDisposer.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 16, 23,
                          "Interceptors and Decorators cannot have methods with parameters annotated with @Disposes.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithDisposerMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);

        // Test quickfix
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te1 = te(22, 24, 22, 34, "");
        CodeAction ca1 = ca(uri, "Remove the '@Disposes' modifier from parameter 'resource'", d1, te1);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca1);
    }

    @Test
    public void validInterceptor() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ValidInterceptor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test that no diagnostics are reported for valid interceptor
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}

// Made with Bob
