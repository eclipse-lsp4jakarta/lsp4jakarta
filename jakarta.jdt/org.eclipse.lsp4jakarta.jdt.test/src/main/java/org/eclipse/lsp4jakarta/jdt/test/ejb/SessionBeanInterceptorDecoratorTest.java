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
 * Tests for session beans annotated with @Interceptor or @Decorator
 */
public class SessionBeanInterceptorDecoratorTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void testSessionBeanWithInterceptorOrDecorator() throws Exception {
        String uri = getJavaFileUri();
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // All 6 invalid session beans should trigger diagnostics
        Diagnostic d1 = d(16, 6, 37,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d2 = d(24, 6, 35,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d3 = d(32, 6, 36,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d4 = d(40, 6, 34,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d5 = d(48, 6, 37,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d6 = d(56, 6, 35,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6);
    }

    @Test
    public void testRemoveStatelessAnnotationQuickFix() throws Exception {
        String uri = getJavaFileUri();
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic diagnostic = d(16, 6, 37,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        // Test quick fix to remove @Stateless annotation
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, diagnostic);
        TextEdit removeStatelessEdit = te(13, 0, 14, 0, "");
        CodeAction removeStatelessAction = ca(uri, "Remove @Stateless", diagnostic, removeStatelessEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeStatelessAction);
    }

    @Test
    public void testRemoveInterceptorAnnotationQuickFix() throws Exception {
        String uri = getJavaFileUri();
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic diagnostic = d(16, 6, 37,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        // Test quick fix to remove @Interceptor annotation
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, diagnostic);
        TextEdit removeInterceptorEdit = te(14, 0, 15, 0, "");
        CodeAction removeInterceptorAction = ca(uri, "Remove @Interceptor", diagnostic, removeInterceptorEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeInterceptorAction);
    }

    @Test
    public void testRemoveStatefulAnnotationQuickFix() throws Exception {
        String uri = getJavaFileUri();
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic diagnostic = d(24, 6, 35,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        // Test quick fix to remove @Stateful annotation (line 21 in the file, but 0-indexed so line 21 becomes 21)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, diagnostic);
        TextEdit removeStatefulEdit = te(21, 0, 22, 0, "");
        CodeAction removeStatefulAction = ca(uri, "Remove @Stateful", diagnostic, removeStatefulEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeStatefulAction);
    }

    @Test
    public void testRemoveDecoratorAnnotationQuickFix() throws Exception {
        String uri = getJavaFileUri();
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic diagnostic = d(24, 6, 35,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        // Test quick fix to remove @Decorator annotation (line 22 in the file)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, diagnostic);
        TextEdit removeDecoratorEdit = te(22, 0, 23, 0, "");
        CodeAction removeDecoratorAction = ca(uri, "Remove @Decorator", diagnostic, removeDecoratorEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeDecoratorAction);
    }

    @Test
    public void testRemoveSingletonAnnotationQuickFix() throws Exception {
        String uri = getJavaFileUri();
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic diagnostic = d(48, 6, 37,
                                  "Session beans must not be annotated with @Interceptor or @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diagnostic);

        // Test quick fix to remove @Singleton annotation (line 45 in the file)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, diagnostic);
        TextEdit removeSingletonEdit = te(45, 0, 46, 0, "");
        CodeAction removeSingletonAction = ca(uri, "Remove @Singleton", diagnostic, removeSingletonEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeSingletonAction);
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
