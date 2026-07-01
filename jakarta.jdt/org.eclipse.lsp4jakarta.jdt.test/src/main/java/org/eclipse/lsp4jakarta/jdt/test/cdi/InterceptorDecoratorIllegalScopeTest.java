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

import java.util.Arrays;

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.ca;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.te;

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

public class InterceptorDecoratorIllegalScopeTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void testInterceptorDecoratorWithIllegalScopes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/InterceptorDecoratorWithIllegalScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Interceptor with @ApplicationScoped (line 49)
        Diagnostic interceptorWithAppScoped = d(49, 6, 38,
                                                "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        interceptorWithAppScoped.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped")));

        // Interceptor with @SessionScoped (line 55)
        Diagnostic interceptorWithSessionScoped = d(55, 6, 34,
                                                    "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        interceptorWithSessionScoped.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped")));

        // Interceptor with multiple scopes (line 62) - TWO diagnostics
        Diagnostic interceptorMultipleScopesDecl = d(62, 6, 42,
                                                     "Scope type annotations must be specified by a managed bean class at most once.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        interceptorMultipleScopesDecl.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped", "jakarta.enterprise.context.ApplicationScoped")));
        Diagnostic interceptorWithMultipleScopes = d(62, 6, 42,
                                                     "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        interceptorWithMultipleScopes.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped", "jakarta.enterprise.context.SessionScoped")));

        // Decorator with @ApplicationScoped (line 70)
        Diagnostic decoratorWithAppScoped = d(70, 6, 36,
                                              "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                              DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        decoratorWithAppScoped.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped")));

        // Decorator with @SessionScoped (line 79)
        Diagnostic decoratorWithSessionScoped = d(79, 6, 32,
                                                  "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        decoratorWithSessionScoped.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped")));

        // Decorator with multiple scopes (line 89) - TWO diagnostics
        Diagnostic decoratorMultipleScopesDecl = d(89, 6, 40,
                                                   "Scope type annotations must be specified by a managed bean class at most once.",
                                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        decoratorMultipleScopesDecl.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ConversationScoped", "jakarta.enterprise.context.RequestScoped")));
        Diagnostic decoratorWithMultipleScopes = d(89, 6, 40,
                                                   "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        decoratorWithMultipleScopes.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped", "jakarta.enterprise.context.ConversationScoped")));

        // Interceptor with custom normal scope (line 100)
        Diagnostic interceptorWithCustomScope = d(100, 6, 38,
                                                  "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        interceptorWithCustomScope.setData(new Gson().toJsonTree(Arrays.asList("io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Decorator with custom normal scope (line 106)
        Diagnostic decoratorWithCustomScope = d(106, 6, 36,
                                                "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        decoratorWithCustomScope.setData(new Gson().toJsonTree(Arrays.asList("io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Interceptor with mixed scopes (line 116) - Only ONE diagnostic (InvalidInterceptorOrDecorator)
        Diagnostic interceptorWithMixedScopes = d(116, 6, 32,
                                                  "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        interceptorWithMixedScopes.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped",
                                                                               "io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        // Decorator with mixed scopes (line 123) - Only ONE diagnostic (InvalidInterceptorOrDecorator)
        Diagnostic decoratorWithMixedScopes = d(123, 6, 30,
                                                "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");
        decoratorWithMixedScopes.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped",
                                                                             "io.openliberty.sample.jakarta.cdi.CustomNormalScope")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, interceptorWithAppScoped, interceptorWithSessionScoped,
                              interceptorMultipleScopesDecl, interceptorWithMultipleScopes, decoratorWithAppScoped, decoratorWithSessionScoped,
                              decoratorMultipleScopesDecl, decoratorWithMultipleScopes, interceptorWithCustomScope, decoratorWithCustomScope,
                              interceptorWithMixedScopes, decoratorWithMixedScopes);

        // Test quickfix for interceptor with @ApplicationScoped (line 41)
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, interceptorWithAppScoped);
        TextEdit replaceWithDependent1 = te(39, 0, 40, 18, "@Dependent\n@Interceptor");
        CodeAction replaceAction1 = ca(uri, "Replace @ApplicationScoped with @Dependent", interceptorWithAppScoped, replaceWithDependent1);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, replaceAction1);

        // Test quickfix for interceptor with @SessionScoped (line 47)
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, interceptorWithSessionScoped);
        TextEdit replaceWithDependent2 = te(45, 0, 46, 14, "@Dependent\n@Interceptor");
        CodeAction replaceAction2 = ca(uri, "Replace @SessionScoped with @Dependent", interceptorWithSessionScoped, replaceWithDependent2);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, replaceAction2);

        // Test quickfix for interceptor with multiple scopes (line 54)
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, interceptorWithMultipleScopes);
        TextEdit replaceWithDependent3 = te(51, 0, 53, 14, "@Dependent\n@Interceptor");
        CodeAction replaceAction3 = ca(uri, "Replace @ApplicationScoped and @SessionScoped with @Dependent", interceptorWithMultipleScopes, replaceWithDependent3);
        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, replaceAction3);

        // Test quickfix for decorator with @ApplicationScoped (line 62)
        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, decoratorWithAppScoped);
        TextEdit replaceWithDependent4 = te(60, 0, 61, 18, "@Dependent\n@Decorator");
        CodeAction replaceAction4 = ca(uri, "Replace @ApplicationScoped with @Dependent", decoratorWithAppScoped, replaceWithDependent4);
        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, replaceAction4);

        // Test quickfix for decorator with multiple scopes (line 75)
        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, decoratorWithMultipleScopes);
        TextEdit replaceWithDependent5 = te(72, 0, 74, 19, "@Dependent\n@Decorator");
        CodeAction replaceAction5 = ca(uri, "Replace @RequestScoped and @ConversationScoped with @Dependent", decoratorWithMultipleScopes, replaceWithDependent5);
        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, replaceAction5);

        // Test quickfix for interceptor with custom normal scope (line 83)
        JakartaJavaCodeActionParams codeActionParams6 = createCodeActionParams(uri, interceptorWithCustomScope);
        TextEdit replaceWithDependent6 = te(81, 0, 82, 18, "@Dependent\n@Interceptor");
        CodeAction replaceAction6 = ca(uri, "Replace @CustomNormalScope with @Dependent", interceptorWithCustomScope, replaceWithDependent6);
        assertJavaCodeAction(codeActionParams6, IJDT_UTILS, replaceAction6);

        // Test quickfix for interceptor with mixed scopes (line 96)
        JakartaJavaCodeActionParams codeActionParams7 = createCodeActionParams(uri, interceptorWithMixedScopes);
        TextEdit replaceWithDependent7 = te(93, 0, 95, 18, "@Dependent\n@Interceptor");
        CodeAction replaceAction7 = ca(uri, "Replace @ApplicationScoped and @CustomNormalScope with @Dependent", interceptorWithMixedScopes, replaceWithDependent7);
        assertJavaCodeAction(codeActionParams7, IJDT_UTILS, replaceAction7);
    }
}
