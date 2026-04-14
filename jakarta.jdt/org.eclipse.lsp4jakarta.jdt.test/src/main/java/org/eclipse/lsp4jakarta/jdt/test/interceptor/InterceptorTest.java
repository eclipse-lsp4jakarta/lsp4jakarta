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
*     IBM Corporation, Archana Iyer R - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.test.interceptor;

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

public class InterceptorTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void invalidInterceptorTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidInterceptor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics
        Diagnostic noArgsConstructorMissingParent = d(5, 13, 31,
                                                      "Missing Public NoArgsConstructor. Class InvalidInterceptor is of Interceptor type, but does not declare a public no-argument constructor.",
                                                      DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorNoArgsConstructorMissing");
        Diagnostic noArgsConstructorMissingChild = d(32, 14, 37,
                                                     "Missing Public NoArgsConstructor. Class InnerInvalidInterceptor is of Interceptor type, but does not declare a public no-argument constructor.",
                                                     DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorNoArgsConstructorMissing");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, noArgsConstructorMissingParent, noArgsConstructorMissingChild);
    }

    @Test
    public void invalidAbstractInterceptorTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidAbstractInterceptor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics
        Diagnostic invalidAbstractModifier = d(5, 22, 48,
                                               "The class InvalidAbstractInterceptor should not contain the abstract modifier. If it contains the abstract modifier, the class should not be annotated with @Interceptor.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorAnnotationOnAbstractClass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, invalidAbstractModifier);
    }

    @Test
    public void invalidInterceptorMethodProceedTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidInterceptorMethodsProceed.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics
        Diagnostic d1 = d(12, 18, 38,
                          "Interceptor methods must always call the InvocationContext.proceed method.",
                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic d2 = d(18, 18, 41,
                          "Interceptor methods must always call the InvocationContext.proceed method.",
                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic d3 = d(24, 18, 39,
                          "Interceptor methods must always call the InvocationContext.proceed method.",
                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic d4 = d(30, 16, 36,
                          "Interceptor methods must always call the InvocationContext.proceed method.",
                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic d5 = d(35, 16, 33,
                          "Interceptor methods must always call the InvocationContext.proceed method.",
                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic d6 = d(46, 19, 44,
                          "Interceptor methods must always call the InvocationContext.proceed method.",
                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic d7 = d(62, 17, 42,
                          "Interceptor methods must always call the InvocationContext.proceed method.",
                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic d8 = d(67, 14, 36,
                          "Interceptor methods must always call the InvocationContext.proceed method.",
                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6, d7, d8);
    }

}
