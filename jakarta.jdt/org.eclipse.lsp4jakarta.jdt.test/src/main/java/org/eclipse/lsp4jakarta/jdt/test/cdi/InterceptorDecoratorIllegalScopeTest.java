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

package org.eclipse.lsp4jakarta.jdt.test.cdi;

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

import com.google.gson.Gson;

public class InterceptorDecoratorIllegalScopeTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void testInterceptorDecoratorWithIllegalScopes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/InterceptorDecoratorWithIllegalScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Interceptor with @ApplicationScoped (line 41)
        Diagnostic d1 = d(41, 6, 38,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d1.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped")));

        // Interceptor with @SessionScoped (line 47)
        Diagnostic d2 = d(47, 6, 34,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d2.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped")));

        // Interceptor with multiple scopes (line 54) - TWO diagnostics
        Diagnostic d3 = d(54, 6, 42,
                          "Scope type annotations must be specified by a managed bean class at most once.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        d3.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped", "jakarta.enterprise.context.ApplicationScoped")));
        Diagnostic d4 = d(54, 6, 42,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d4.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped", "jakarta.enterprise.context.SessionScoped")));

        // Decorator with @ApplicationScoped (line 62)
        Diagnostic d5 = d(62, 6, 36,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d5.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped")));

        // Decorator with @SessionScoped (line 68)
        Diagnostic d6 = d(68, 6, 32,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d6.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped")));

        // Decorator with multiple scopes (line 75) - TWO diagnostics
        Diagnostic d7 = d(75, 6, 40,
                          "Scope type annotations must be specified by a managed bean class at most once.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        d7.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ConversationScoped", "jakarta.enterprise.context.RequestScoped")));
        Diagnostic d8 = d(75, 6, 40,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d8.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped", "jakarta.enterprise.context.ConversationScoped")));

        // Interceptor with custom normal scope (line 83)
        Diagnostic d9 = d(83, 6, 38,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d9.setData(new Gson().toJsonTree(Arrays.asList("io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Decorator with custom normal scope (line 89)
        Diagnostic d10 = d(89, 6, 36,
                           "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                           DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d10.setData(new Gson().toJsonTree(Arrays.asList("io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Interceptor with mixed scopes (line 96) - Only ONE diagnostic (InvalidInterceptorOrDecorator)
        Diagnostic d11 = d(96, 6, 32,
                           "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                           DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d11.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped", "io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Decorator with mixed scopes (line 103) - Only ONE diagnostic (InvalidInterceptorOrDecorator)
        Diagnostic d12 = d(103, 6, 30,
                           "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                           DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d12.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped", "io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12);
    }
}
