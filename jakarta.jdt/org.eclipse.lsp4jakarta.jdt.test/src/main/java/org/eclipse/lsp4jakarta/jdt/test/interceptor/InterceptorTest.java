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

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, invalidAbstractModifier);
        TextEdit te = te(5, 6, 5, 15, "");
        CodeAction ca = ca(uri, "Remove the 'abstract' modifier", invalidAbstractModifier, te);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca);
    }

    @Test
    public void invalidInterceptorWithObserverMethodTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidInterceptorWithObserverMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics for @Observes
        Diagnostic observesMethodDiagnostic = d(13, 16, 30,
                                                "Interceptors and Decorators cannot have methods with parameters annotated with @Observes or @ObservesAsync.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithObserverMethod");

        // Test diagnostics for @ObservesAsync
        Diagnostic observesAsyncMethodDiagnostic = d(18, 16, 35,
                                                     "Interceptors and Decorators cannot have methods with parameters annotated with @Observes or @ObservesAsync.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithObserverMethod");

        // Test diagnostics for both @Observes and @ObservesAsync
        Diagnostic observesBothMethodDiagnostic = d(23, 16, 34,
                                                    "Interceptors and Decorators cannot have methods with parameters annotated with @Observes or @ObservesAsync.",
                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithObserverMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, observesMethodDiagnostic, observesAsyncMethodDiagnostic, observesBothMethodDiagnostic);
    }

    @Test
    public void invalidDecoratorWithObserverMethodTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidDecoratorWithObserverMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics for @Observes
        Diagnostic observesMethodDiagnostic = d(13, 16, 30,
                                                "Interceptors and Decorators cannot have methods with parameters annotated with @Observes or @ObservesAsync.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithObserverMethod");

        // Test diagnostics for @ObservesAsync
        Diagnostic observesAsyncMethodDiagnostic = d(18, 16, 35,
                                                     "Interceptors and Decorators cannot have methods with parameters annotated with @Observes or @ObservesAsync.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithObserverMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, observesMethodDiagnostic, observesAsyncMethodDiagnostic);
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
        Diagnostic aroundInvokeInvalidProceed = d(17, 18, 38,
                                                  "Interceptor methods must always call the InvocationContext.proceed method.",
                                                  DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic aroundConstructInvalidProceed = d(23, 18, 41,
                                                     "Interceptor methods must always call the InvocationContext.proceed method.",
                                                     DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic aroundTimeoutInvalidProceed = d(29, 18, 39,
                                                   "Interceptor methods must always call the InvocationContext.proceed method.",
                                                   DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic postConstructInvalidProceed = d(35, 16, 36,
                                                   "Interceptor methods must always call the InvocationContext.proceed method.",
                                                   DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic preDestroyInvalidProceed = d(40, 16, 33,
                                                "Interceptor methods must always call the InvocationContext.proceed method.",
                                                DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic aroundInvokeInvalidProceedChild = d(51, 19, 44,
                                                       "Interceptor methods must always call the InvocationContext.proceed method.",
                                                       DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic postConstructInvalidProceedChild = d(67, 17, 42,
                                                        "Interceptor methods must always call the InvocationContext.proceed method.",
                                                        DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");
        Diagnostic preDestroyInvalidProceedChild = d(72, 14, 36,
                                                     "Interceptor methods must always call the InvocationContext.proceed method.",
                                                     DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, aroundInvokeInvalidProceed, aroundConstructInvalidProceed,
                              aroundTimeoutInvalidProceed, postConstructInvalidProceed, preDestroyInvalidProceed, aroundInvokeInvalidProceedChild,
                              postConstructInvalidProceedChild, preDestroyInvalidProceedChild);
    }

}
