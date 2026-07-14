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
 * Tests for session beans annotated with @Interceptor or @Decorator.
 * Each test method targets a single source file under the
 * ejb/interceptordecorator sub-package.
 */
public class SessionBeanInterceptorDecoratorTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -----------------------------------------------------------------------
    // @Stateless + @Interceptor
    // -----------------------------------------------------------------------

    @Test
    public void testStatelessWithInterceptor() throws Exception {
        String uri = getFileUri("InvalidStatelessWithInterceptor.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // class InvalidStatelessWithInterceptor { — row 8, cols 6–37
        Diagnostic diagnostic = d(8, 6, 37,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, diagnostic);
        // Remove @Interceptor: from end of @Stateless (row 6, col 10) to end of @Interceptor (row 7, col 12)
        TextEdit removeInterceptorEdit = te(6, 10, 7, 12, "");
        CodeAction removeInterceptorAction = ca(uri, "Remove @Interceptor", diagnostic, removeInterceptorEdit);
        // Remove @Stateless: from start of @Stateless (row 6) to start of @Interceptor (row 7)
        TextEdit removeStatelessEdit = te(6, 0, 7, 0, "");
        CodeAction removeStatelessAction = ca(uri, "Remove @Stateless", diagnostic, removeStatelessEdit);
        assertJavaCodeAction(params, IJDT_UTILS, removeInterceptorAction, removeStatelessAction);
    }

    // -----------------------------------------------------------------------
    // @Stateless + @Decorator
    // -----------------------------------------------------------------------

    @Test
    public void testStatelessWithDecorator() throws Exception {
        String uri = getFileUri("InvalidStatelessWithDecorator.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // class InvalidStatelessWithDecorator { — row 10, cols 6–35
        Diagnostic diagnostic = d(10, 6, 35,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, diagnostic);
        // Remove @Stateless: from start of @Stateless (row 8) to start of @Decorator (row 9)
        TextEdit removeStatelessEdit = te(8, 0, 9, 0, "");
        CodeAction removeStatelessAction = ca(uri, "Remove @Stateless", diagnostic, removeStatelessEdit);
        // Remove @Decorator: from end of @Stateless (row 8, col 10) to end of @Decorator (row 9, col 10)
        TextEdit removeDecoratorEdit = te(8, 10, 9, 10, "");
        CodeAction removeDecoratorAction = ca(uri, "Remove @Decorator", diagnostic, removeDecoratorEdit);
        assertJavaCodeAction(params, IJDT_UTILS, removeDecoratorAction, removeStatelessAction);
    }

    // -----------------------------------------------------------------------
    // @Stateful + @Interceptor
    // -----------------------------------------------------------------------

    @Test
    public void testStatefulWithInterceptor() throws Exception {
        String uri = getFileUri("InvalidStatefulWithInterceptor.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // class InvalidStatefulWithInterceptor { — row 8, cols 6–36
        Diagnostic diagnostic = d(8, 6, 36,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, diagnostic);
        // Remove @Interceptor: from end of @Stateful (row 6, col 9) to end of @Interceptor (row 7, col 12)
        TextEdit removeInterceptorEdit = te(6, 9, 7, 12, "");
        CodeAction removeInterceptorAction = ca(uri, "Remove @Interceptor", diagnostic, removeInterceptorEdit);
        // Remove @Stateful: from start of @Stateful (row 6) to start of @Interceptor (row 7)
        TextEdit removeStatefulEdit = te(6, 0, 7, 0, "");
        CodeAction removeStatefulAction = ca(uri, "Remove @Stateful", diagnostic, removeStatefulEdit);
        assertJavaCodeAction(params, IJDT_UTILS, removeInterceptorAction, removeStatefulAction);
    }

    // -----------------------------------------------------------------------
    // @Stateful + @Decorator
    // -----------------------------------------------------------------------

    @Test
    public void testStatefulWithDecorator() throws Exception {
        String uri = getFileUri("InvalidStatefulWithDecorator.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // class InvalidStatefulWithDecorator { — row 10, cols 6–34
        Diagnostic diagnostic = d(10, 6, 34,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, diagnostic);
        // Remove @Stateful: from start of @Stateful (row 8) to start of @Decorator (row 9)
        TextEdit removeStatefulEdit = te(8, 0, 9, 0, "");
        CodeAction removeStatefulAction = ca(uri, "Remove @Stateful", diagnostic, removeStatefulEdit);
        // Remove @Decorator: from end of @Stateful (row 8, col 9) to end of @Decorator (row 9, col 10)
        TextEdit removeDecoratorEdit = te(8, 9, 9, 10, "");
        CodeAction removeDecoratorAction = ca(uri, "Remove @Decorator", diagnostic, removeDecoratorEdit);
        assertJavaCodeAction(params, IJDT_UTILS, removeDecoratorAction, removeStatefulAction);
    }

    // -----------------------------------------------------------------------
    // @Singleton + @Interceptor
    // -----------------------------------------------------------------------

    @Test
    public void testSingletonWithInterceptor() throws Exception {
        String uri = getFileUri("InvalidSingletonWithInterceptor.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // class InvalidSingletonWithInterceptor { — row 8, cols 6–37
        Diagnostic diagnostic = d(8, 6, 37,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, diagnostic);
        // Remove @Interceptor: from end of @Singleton (row 6, col 10) to end of @Interceptor (row 7, col 12)
        TextEdit removeInterceptorEdit = te(6, 10, 7, 12, "");
        CodeAction removeInterceptorAction = ca(uri, "Remove @Interceptor", diagnostic, removeInterceptorEdit);
        // Remove @Singleton: from start of @Singleton (row 6) to start of @Interceptor (row 7)
        TextEdit removeSingletonEdit = te(6, 0, 7, 0, "");
        CodeAction removeSingletonAction = ca(uri, "Remove @Singleton", diagnostic, removeSingletonEdit);
        assertJavaCodeAction(params, IJDT_UTILS, removeInterceptorAction, removeSingletonAction);
    }

    // -----------------------------------------------------------------------
    // @Singleton + @Decorator
    // -----------------------------------------------------------------------

    @Test
    public void testSingletonWithDecorator() throws Exception {
        String uri = getFileUri("InvalidSingletonWithDecorator.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // class InvalidSingletonWithDecorator { — row 10, cols 6–35
        Diagnostic diagnostic = d(10, 6, 35,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, diagnostic);
        // Remove @Singleton: from start of @Singleton (row 8) to start of @Decorator (row 9)
        TextEdit removeSingletonEdit = te(8, 0, 9, 0, "");
        CodeAction removeSingletonAction = ca(uri, "Remove @Singleton", diagnostic, removeSingletonEdit);
        // Remove @Decorator: from end of @Singleton (row 8, col 10) to end of @Decorator (row 9, col 10)
        TextEdit removeDecoratorEdit = te(8, 10, 9, 10, "");
        CodeAction removeDecoratorAction = ca(uri, "Remove @Decorator", diagnostic, removeDecoratorEdit);
        assertJavaCodeAction(params, IJDT_UTILS, removeDecoratorAction, removeSingletonAction);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private JakartaJavaDiagnosticsParams createDiagnosticsParams(String uri) {
        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        return diagnosticsParams;
    }

    private String getFileUri(String fileName) throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/ejb/interceptordecorator/" + fileName));
        return javaFile.getLocation().toFile().toURI().toString();
    }
}
