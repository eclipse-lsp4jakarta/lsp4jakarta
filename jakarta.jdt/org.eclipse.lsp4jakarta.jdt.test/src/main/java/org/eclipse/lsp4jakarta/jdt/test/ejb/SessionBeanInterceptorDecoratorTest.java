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

        // @Stateless with @Interceptor
        Diagnostic statelessWithInterceptorDiagnostic = d(17, 6, 37,
                                                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                                                          DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        // @Stateless with @Decorator
        Diagnostic statelessWithDecoratorDiagnostic = d(25, 6, 35,
                                                        "Session beans must not be annotated with @Interceptor or @Decorator.",
                                                        DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        // @Stateful with @Interceptor
        Diagnostic statefulWithInterceptorDiagnostic = d(36, 6, 36,
                                                         "Session beans must not be annotated with @Interceptor or @Decorator.",
                                                         DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        // @Stateful with @Decorator
        Diagnostic statefulWithDecoratorDiagnostic = d(44, 6, 34,
                                                       "Session beans must not be annotated with @Interceptor or @Decorator.",
                                                       DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        // @Singleton with @Interceptor
        Diagnostic singletonWithInterceptorDiagnostic = d(55, 6, 37,
                                                          "Session beans must not be annotated with @Interceptor or @Decorator.",
                                                          DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        // @Singleton with @Decorator
        Diagnostic singletonWithDecoratorDiagnostic = d(63, 6, 35,
                                                        "Session beans must not be annotated with @Interceptor or @Decorator.",
                                                        DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionBeanWithInterceptorOrDecorator");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              statelessWithInterceptorDiagnostic,
                              statelessWithDecoratorDiagnostic,
                              statefulWithInterceptorDiagnostic,
                              statefulWithDecoratorDiagnostic,
                              singletonWithInterceptorDiagnostic,
                              singletonWithDecoratorDiagnostic);

        // Test code actions for @Stateless with @Interceptor
        JakartaJavaCodeActionParams statelessInterceptorParams = createCodeActionParams(uri, statelessWithInterceptorDiagnostic);
        TextEdit removeInterceptorFromStatelessEdit = te(15, 10, 16, 12, "");
        CodeAction removeInterceptorFromStatelessAction = ca(uri, "Remove @Interceptor", statelessWithInterceptorDiagnostic, removeInterceptorFromStatelessEdit);
        TextEdit removeStatelessWithInterceptorEdit = te(14, 0, 16, 0, "");
        CodeAction removeStatelessWithInterceptorAction = ca(uri, "Remove @Stateless", statelessWithInterceptorDiagnostic, removeStatelessWithInterceptorEdit);
        assertJavaCodeAction(statelessInterceptorParams, IJDT_UTILS, removeInterceptorFromStatelessAction, removeStatelessWithInterceptorAction);

        // Test code actions for @Stateless with @Decorator
        JakartaJavaCodeActionParams statelessDecoratorParams = createCodeActionParams(uri, statelessWithDecoratorDiagnostic);
        TextEdit removeStatelessWithDecoratorEdit = te(23, 0, 24, 0, "");
        CodeAction removeStatelessWithDecoratorAction = ca(uri, "Remove @Stateless", statelessWithDecoratorDiagnostic, removeStatelessWithDecoratorEdit);
        TextEdit removeDecoratorFromStatelessEdit = te(23, 10, 24, 10, "");
        CodeAction removeDecoratorFromStatelessAction = ca(uri, "Remove @Decorator", statelessWithDecoratorDiagnostic, removeDecoratorFromStatelessEdit);
        assertJavaCodeAction(statelessDecoratorParams, IJDT_UTILS, removeDecoratorFromStatelessAction, removeStatelessWithDecoratorAction);

        // Test code actions for @Singleton with @Interceptor
        JakartaJavaCodeActionParams singletonInterceptorParams = createCodeActionParams(uri, singletonWithInterceptorDiagnostic);
        TextEdit removeInterceptorFromSingletonEdit = te(53, 10, 54, 12, "");
        CodeAction removeInterceptorFromSingletonAction = ca(uri, "Remove @Interceptor", singletonWithInterceptorDiagnostic, removeInterceptorFromSingletonEdit);
        TextEdit removeSingletonWithInterceptorEdit = te(53, 0, 54, 0, "");
        CodeAction removeSingletonWithInterceptorAction = ca(uri, "Remove @Singleton", singletonWithInterceptorDiagnostic, removeSingletonWithInterceptorEdit);
        assertJavaCodeAction(singletonInterceptorParams, IJDT_UTILS, removeInterceptorFromSingletonAction, removeSingletonWithInterceptorAction);
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
