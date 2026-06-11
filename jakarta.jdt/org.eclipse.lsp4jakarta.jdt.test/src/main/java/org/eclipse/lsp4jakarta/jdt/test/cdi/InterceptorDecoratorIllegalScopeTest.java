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
import com.google.gson.Gson;
import org.junit.Test;

public class InterceptorDecoratorIllegalScopeTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void testInterceptorWithIllegalScopes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/InterceptorDecoratorWithIllegalScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test interceptor with @ApplicationScoped - should trigger diagnostic
        Diagnostic applicationScopedDiagnostic = d(41, 6, 42,
                                                   "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Test interceptor with @SessionScoped - should trigger diagnostic
        Diagnostic sessionScopedDiagnostic = d(47, 6, 38,
                                               "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Test interceptor with @RequestScoped - should trigger diagnostic
        Diagnostic requestScopedDiagnostic = d(53, 6, 38,
                                               "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Test interceptor with @ConversationScoped - should trigger diagnostic
        Diagnostic conversationScopedDiagnostic = d(59, 6, 43,
                                                    "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Test interceptor with multiple illegal scopes - triggers TWO diagnostics
        // One for multiple scopes, one for illegal scope on interceptor
        Diagnostic multipleScopesDiagnostic = d(66, 6, 42,
                                                "Scope type annotations must be specified by a managed bean class at most once.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        multipleScopesDiagnostic.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped", "jakarta.enterprise.context.ApplicationScoped")));
        Diagnostic multipleScopesIllegalDiagnostic = d(66, 6, 42,
                                                       "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Valid interceptors with @Dependent or no scope should not trigger diagnostics
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, applicationScopedDiagnostic, sessionScopedDiagnostic,
                              requestScopedDiagnostic, conversationScopedDiagnostic, multipleScopesDiagnostic, multipleScopesIllegalDiagnostic);

        // Test code action for interceptor with @ApplicationScoped
        JakartaJavaCodeActionParams applicationScopedParams = createCodeActionParams(uri, applicationScopedDiagnostic);
        TextEdit applicationScopedEdit = te(40, 0, 41, 18, "@Dependent");
        CodeAction applicationScopedAction = ca(uri, "Replace current scope with @Dependent", applicationScopedDiagnostic, applicationScopedEdit);
        assertJavaCodeAction(applicationScopedParams, IJDT_UTILS, applicationScopedAction);

        // Test code action for interceptor with @SessionScoped
        JakartaJavaCodeActionParams sessionScopedParams = createCodeActionParams(uri, sessionScopedDiagnostic);
        TextEdit sessionScopedEdit = te(46, 0, 47, 14, "@Dependent");
        CodeAction sessionScopedAction = ca(uri, "Replace current scope with @Dependent", sessionScopedDiagnostic, sessionScopedEdit);
        assertJavaCodeAction(sessionScopedParams, IJDT_UTILS, sessionScopedAction);

        // Test code action for interceptor with @RequestScoped
        JakartaJavaCodeActionParams requestScopedParams = createCodeActionParams(uri, requestScopedDiagnostic);
        TextEdit requestScopedEdit = te(52, 0, 53, 14, "@Dependent");
        CodeAction requestScopedAction = ca(uri, "Replace current scope with @Dependent", requestScopedDiagnostic, requestScopedEdit);
        assertJavaCodeAction(requestScopedParams, IJDT_UTILS, requestScopedAction);

        // Test code action for interceptor with @ConversationScoped
        JakartaJavaCodeActionParams conversationScopedParams = createCodeActionParams(uri, conversationScopedDiagnostic);
        TextEdit conversationScopedEdit = te(58, 0, 59, 19, "@Dependent");
        CodeAction conversationScopedAction = ca(uri, "Replace current scope with @Dependent", conversationScopedDiagnostic, conversationScopedEdit);
        assertJavaCodeAction(conversationScopedParams, IJDT_UTILS, conversationScopedAction);

        // Test code action for interceptor with multiple illegal scopes
        JakartaJavaCodeActionParams multipleScopesParams = createCodeActionParams(uri, multipleScopesIllegalDiagnostic);
        TextEdit multipleScopesEdit = te(64, 0, 66, 14, "@Dependent");
        CodeAction multipleScopesAction = ca(uri, "Replace current scope with @Dependent", multipleScopesIllegalDiagnostic, multipleScopesEdit);
        assertJavaCodeAction(multipleScopesParams, IJDT_UTILS, multipleScopesAction);
    }

    @Test
    public void testDecoratorWithIllegalScopes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/InterceptorDecoratorWithIllegalScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test decorator with @ApplicationScoped - should trigger diagnostic
        Diagnostic applicationScopedDiagnostic = d(74, 6, 40,
                                                   "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Test decorator with @SessionScoped - should trigger diagnostic
        Diagnostic sessionScopedDiagnostic = d(80, 6, 36,
                                               "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Test decorator with @RequestScoped - should trigger diagnostic
        Diagnostic requestScopedDiagnostic = d(86, 6, 36,
                                               "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Test decorator with @ConversationScoped - should trigger diagnostic
        Diagnostic conversationScopedDiagnostic = d(92, 6, 41,
                                                    "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Test decorator with multiple illegal scopes - triggers TWO diagnostics
        // One for multiple scopes, one for illegal scope on decorator
        Diagnostic multipleScopesDiagnostic = d(99, 6, 40,
                                                "Scope type annotations must be specified by a managed bean class at most once.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        multipleScopesDiagnostic.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ConversationScoped", "jakarta.enterprise.context.RequestScoped")));
        Diagnostic multipleScopesIllegalDiagnostic = d(99, 6, 40,
                                                       "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Valid decorators with @Dependent or no scope should not trigger diagnostics
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, applicationScopedDiagnostic, sessionScopedDiagnostic,
                              requestScopedDiagnostic, conversationScopedDiagnostic, multipleScopesDiagnostic, multipleScopesIllegalDiagnostic);

        // Test code action for decorator with @ApplicationScoped
        JakartaJavaCodeActionParams applicationScopedParams = createCodeActionParams(uri, applicationScopedDiagnostic);
        TextEdit applicationScopedEdit = te(73, 0, 74, 18, "@Dependent");
        CodeAction applicationScopedAction = ca(uri, "Replace current scope with @Dependent", applicationScopedDiagnostic, applicationScopedEdit);
        assertJavaCodeAction(applicationScopedParams, IJDT_UTILS, applicationScopedAction);

        // Test code action for decorator with @SessionScoped
        JakartaJavaCodeActionParams sessionScopedParams = createCodeActionParams(uri, sessionScopedDiagnostic);
        TextEdit sessionScopedEdit = te(79, 0, 80, 14, "@Dependent");
        CodeAction sessionScopedAction = ca(uri, "Replace current scope with @Dependent", sessionScopedDiagnostic, sessionScopedEdit);
        assertJavaCodeAction(sessionScopedParams, IJDT_UTILS, sessionScopedAction);

        // Test code action for decorator with @RequestScoped
        JakartaJavaCodeActionParams requestScopedParams = createCodeActionParams(uri, requestScopedDiagnostic);
        TextEdit requestScopedEdit = te(85, 0, 86, 14, "@Dependent");
        CodeAction requestScopedAction = ca(uri, "Replace current scope with @Dependent", requestScopedDiagnostic, requestScopedEdit);
        assertJavaCodeAction(requestScopedParams, IJDT_UTILS, requestScopedAction);

        // Test code action for decorator with @ConversationScoped
        JakartaJavaCodeActionParams conversationScopedParams = createCodeActionParams(uri, conversationScopedDiagnostic);
        TextEdit conversationScopedEdit = te(91, 0, 92, 19, "@Dependent");
        CodeAction conversationScopedAction = ca(uri, "Replace current scope with @Dependent", conversationScopedDiagnostic, conversationScopedEdit);
        assertJavaCodeAction(conversationScopedParams, IJDT_UTILS, conversationScopedAction);

        // Test code action for decorator with multiple illegal scopes
        JakartaJavaCodeActionParams multipleScopesParams = createCodeActionParams(uri, multipleScopesIllegalDiagnostic);
        TextEdit multipleScopesEdit = te(97, 0, 99, 19, "@Dependent");
        CodeAction multipleScopesAction = ca(uri, "Replace current scope with @Dependent", multipleScopesIllegalDiagnostic, multipleScopesEdit);
        assertJavaCodeAction(multipleScopesParams, IJDT_UTILS, multipleScopesAction);
    }

    @Test
    public void testInterceptorDecoratorWithCustomNormalScope() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/InterceptorDecoratorWithIllegalScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for interceptor with custom normal scope (line 83)
        Diagnostic d1 = d(83, 6, 36,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d1.setData(new Gson().toJsonTree(Arrays.asList("io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Test diagnostic for decorator with custom normal scope (line 89)
        Diagnostic d2 = d(89, 6, 34,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d2.setData(new Gson().toJsonTree(Arrays.asList("io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Test diagnostic for interceptor with mixed scopes (built-in + custom) (line 96)
        Diagnostic d3 = d(96, 6, 32,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d3.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped", "io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Test diagnostic for decorator with mixed scopes (built-in + custom) (line 103)
        Diagnostic d4 = d(103, 6, 30,
                          "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        d4.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped", "io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4);

        // Test code action for interceptor with custom normal scope (line 83)
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(82, 0, 83, 17, "@Dependent");
        CodeAction ca1 = ca(uri, "Replace @CustomNormalScope with @Dependent", d1, te1);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1);

        // Test code action for decorator with custom normal scope (line 89)
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te2 = te(88, 0, 89, 17, "@Dependent");
        CodeAction ca2 = ca(uri, "Replace @CustomNormalScope with @Dependent", d2, te2);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca2);

        // Test code action for interceptor with mixed scopes (lines 95-96)
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);
        TextEdit te3 = te(94, 0, 96, 17, "@Dependent");
        CodeAction ca3 = ca(uri, "Replace @ApplicationScoped, @CustomNormalScope with @Dependent", d3, te3);
        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca3);

        // Test code action for decorator with mixed scopes (lines 102-103)
        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, d4);
        TextEdit te4 = te(101, 0, 103, 17, "@Dependent");
        CodeAction ca4 = ca(uri, "Replace @ApplicationScoped, @CustomNormalScope with @Dependent", d4, te4);
        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, ca4);
    }
}
