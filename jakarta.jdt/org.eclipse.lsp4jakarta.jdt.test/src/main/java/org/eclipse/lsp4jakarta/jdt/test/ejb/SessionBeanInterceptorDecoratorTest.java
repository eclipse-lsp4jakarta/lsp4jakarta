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

package org.eclipse.lsp4jakarta.jdt.test.ejb;

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
 * Tests for session beans annotated with @Interceptor or @Decorator
 */
public class SessionBeanInterceptorDecoratorTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void testSessionBeanWithInterceptorOrDecorator() throws Exception {
        String uri = getJavaFileUri();
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // All 6 invalid session beans should trigger diagnostics
        Diagnostic d1 = d(15, 6, 37,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d2 = d(23, 6, 35,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d3 = d(31, 6, 36,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d4 = d(39, 6, 34,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d5 = d(47, 6, 37,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d6 = d(55, 6, 35,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6);
    }

    private JakartaJavaDiagnosticsParams createDiagnosticsParams(String uri) {
        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        return diagnosticsParams;
    }

    private String getJavaFileUri() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/ejb/SessionBeanInterceptorDecorator.java"));
        return javaFile.getLocation().toFile().toURI().toString();
    }
}

// Made with Bob
