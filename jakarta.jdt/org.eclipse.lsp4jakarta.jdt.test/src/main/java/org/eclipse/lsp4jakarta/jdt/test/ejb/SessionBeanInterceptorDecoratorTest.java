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

        Diagnostic d1 = d(17, 6, 37,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d2 = d(25, 6, 35,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d3 = d(36, 6, 36,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d4 = d(44, 6, 34,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d5 = d(55, 6, 37,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        Diagnostic d6 = d(63, 6, 35,
                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit removeInterceptorEdit = te(15, 10, 16, 12, "");
        CodeAction removeInterceptorAction = ca(uri, "Remove @Interceptor", d1, removeInterceptorEdit);
        TextEdit removeStatelessEdit = te(14, 0, 16, 0, "");
        CodeAction removeStatelessAction = ca(uri, "Remove @Stateless", d1, removeStatelessEdit);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, removeInterceptorAction, removeStatelessAction);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit removeStatelessEdit2 = te(23, 0, 24, 0, "");
        CodeAction removeStatelessAction2 = ca(uri, "Remove @Stateless", d2, removeStatelessEdit2);
        TextEdit removeDecoratorEdit = te(23, 10, 24, 10, "");
        CodeAction removeDecoratorAction = ca(uri, "Remove @Decorator", d2, removeDecoratorEdit);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, removeDecoratorAction, removeStatelessAction2);

        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, d5);
        TextEdit removeInterceptorEdit5 = te(53, 10, 54, 12, "");
        CodeAction removeInterceptorAction5 = ca(uri, "Remove @Interceptor", d5, removeInterceptorEdit5);
        TextEdit removeSingletonEdit = te(53, 0, 54, 0, "");
        CodeAction removeSingletonAction = ca(uri, "Remove @Singleton", d5, removeSingletonEdit);
        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, removeInterceptorAction5, removeSingletonAction);
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
