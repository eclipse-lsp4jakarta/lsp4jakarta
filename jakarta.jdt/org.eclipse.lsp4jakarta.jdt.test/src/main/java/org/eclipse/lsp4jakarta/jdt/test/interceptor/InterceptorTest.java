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
import com.google.gson.Gson;
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
    private static final String PROCEED_NOT_IN_TRY_CATCH_MSG = "Exceptions and initialization and/or cleanup operations should typically be handled in try/catch/finally blocks around the proceed method.";

    @Test
    public void invalidInterceptorTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidInterceptor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics
        Diagnostic noArgsConstructorMissingParent = d(6, 13, 31,
                                                      "Missing Public NoArgsConstructor. Class InvalidInterceptor is of Interceptor type, but does not declare a public no-argument constructor.",
                                                      DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorNoArgsConstructorMissing");
        Diagnostic noArgsConstructorMissingChild = d(34, 14, 37,
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
        Diagnostic observesMethodDiagnostic = d(14, 16, 30,
                                                "Interceptors and Decorators cannot have methods with parameters annotated with @Observes or @ObservesAsync.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithObserverMethod");

        // Test diagnostics for @ObservesAsync
        Diagnostic observesAsyncMethodDiagnostic = d(19, 16, 35,
                                                     "Interceptors and Decorators cannot have methods with parameters annotated with @Observes or @ObservesAsync.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecoratorWithObserverMethod");

        // Test diagnostics for both @Observes and @ObservesAsync
        Diagnostic observesBothMethodDiagnostic = d(24, 16, 34,
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

        // Test diagnostic for missing @Delegate (from DecoratorDiagnosticsParticipant)
        Diagnostic noDelegateDiagnostic = d(7, 13, 47,
                                            "A decorator must declare exactly one injection point annotated with @Delegate.",
                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidDecoratorDelegateInjectionPoints");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, observesMethodDiagnostic, observesAsyncMethodDiagnostic, noDelegateDiagnostic);
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

    @Test
    public void invalidAroundInvokeMethodModifiersTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidAroundInvokeMethods.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics for invalid method modifiers
        Diagnostic finalModifierDiagnostic = d(8, 24, 32,
                                               "AroundInvoke interceptor method must not be declared as a final method.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnFinalMethod",
                                               new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundInvoke")));

        Diagnostic abstractModifierDiagnostic = d(13, 27, 38,
                                                  "AroundInvoke interceptor method must not be declared as an abstract method.",
                                                  DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnAbstractMethod",
                                                  new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundInvoke")));

        Diagnostic duplicateAroundInvoke1 = d(13, 27, 38,
                                              "Only one method with @AroundInvoke annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                              DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic proceedDiagnostic = d(13, 27, 38,
                                         "Interceptor methods must always call the InvocationContext.proceed method.",
                                         DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");

        Diagnostic staticModifierDiagnostic = d(16, 25, 34,
                                                "AroundInvoke interceptor method must not be declared as a static method.",
                                                DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnStaticMethod",
                                                new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundInvoke")));

        Diagnostic invalidAbstractClassDiagnostic = d(5, 22, 48,
                                                      "The class InvalidAroundInvokeMethods should not contain the abstract modifier. If it contains the abstract modifier, the class should not be annotated with @Interceptor.",
                                                      DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorAnnotationOnAbstractClass");

        // Test diagnostics for duplicate interceptor methods
        Diagnostic duplicateAroundInvoke2 = d(16, 25, 34,
                                              "Only one method with @AroundInvoke annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                              DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic duplicateAroundInvoke3 = d(21, 18, 26,
                                              "Only one method with @AroundInvoke annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                              DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // InvalidInterceptorProceedNotInTryCatch warnings: logFinal (8,24,32), logStatic (16,25,34), logValid (21,18,26)
        Diagnostic proceedNotInTryCatchFinal = d(8, 24, 32,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchStatic = d(16, 25, 34,
                                                  PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                  DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchValid = d(21, 18, 26,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, finalModifierDiagnostic, abstractModifierDiagnostic, duplicateAroundInvoke1,
                              proceedDiagnostic, staticModifierDiagnostic, invalidAbstractClassDiagnostic, duplicateAroundInvoke2, duplicateAroundInvoke3,
                              proceedNotInTryCatchFinal, proceedNotInTryCatchStatic, proceedNotInTryCatchValid);

        // Test code actions for final modifier
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, finalModifierDiagnostic);
        TextEdit removeAroundInvokeOnFinalEdit = te(7, 1, 8, 4, "");
        TextEdit removeFinalEdit = te(8, 10, 8, 16, "");
        CodeAction removeAroundInvokeOnFinalAction = ca(uri, "Remove @AroundInvoke", finalModifierDiagnostic, removeAroundInvokeOnFinalEdit);
        CodeAction removeFinalAction = ca(uri, "Remove the 'final' modifier", finalModifierDiagnostic, removeFinalEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAroundInvokeOnFinalAction, removeFinalAction);

        // Test code actions for abstract modifier
        codeActionParams = createCodeActionParams(uri, abstractModifierDiagnostic);
        TextEdit removeAroundInvokeOnAbstractEdit = te(12, 4, 13, 4, "");
        TextEdit removeAbstractEdit = te(13, 10, 13, 19, "");
        CodeAction removeAroundInvokeOnAbstractAction = ca(uri, "Remove @AroundInvoke", abstractModifierDiagnostic, removeAroundInvokeOnAbstractEdit);
        CodeAction removeAbstractAction = ca(uri, "Remove the 'abstract' modifier", abstractModifierDiagnostic, removeAbstractEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAroundInvokeOnAbstractAction, removeAbstractAction);

        // Test code actions for static modifier
        codeActionParams = createCodeActionParams(uri, staticModifierDiagnostic);
        TextEdit removeAroundInvokeOnStaticEdit = te(15, 4, 16, 4, "");
        TextEdit removeStaticEdit = te(16, 10, 16, 17, "");
        CodeAction removeAroundInvokeOnStaticAction = ca(uri, "Remove @AroundInvoke", staticModifierDiagnostic, removeAroundInvokeOnStaticEdit);
        CodeAction removeStaticAction = ca(uri, "Remove the 'static' modifier", staticModifierDiagnostic, removeStaticEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAroundInvokeOnStaticAction, removeStaticAction);
    }

    @Test
    public void invalidAroundConstructMethodModifiersTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidAroundConstructMethods.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics for invalid method modifiers
        Diagnostic finalModifierDiagnostic = d(8, 24, 32,
                                               "AroundConstruct interceptor method must not be declared as a final method.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnFinalMethod",
                                               new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundConstruct")));

        Diagnostic abstractModifierDiagnostic = d(13, 27, 38,
                                                  "AroundConstruct interceptor method must not be declared as an abstract method.",
                                                  DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnAbstractMethod",
                                                  new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundConstruct")));

        Diagnostic duplicateAroundConstruct1 = d(13, 27, 38,
                                                 "Only one method with @AroundConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                                 DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic proceedDiagnostics = d(13, 27, 38,
                                          "Interceptor methods must always call the InvocationContext.proceed method.",
                                          DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");

        Diagnostic staticModifierDiagnostic = d(16, 25, 34,
                                                "AroundConstruct lifecycle callback interceptor method must not be declared as static except in an application client.",
                                                DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnStaticMethod",
                                                new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundConstruct")));

        Diagnostic invalidAbstractClassDiagnostics = d(5, 22, 51,
                                                       "The class InvalidAroundConstructMethods should not contain the abstract modifier. If it contains the abstract modifier, the class should not be annotated with @Interceptor.",
                                                       DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorAnnotationOnAbstractClass");

        Diagnostic invalidMulipleModifierFinalDiagnostics = d(21, 31, 50,
                                                              "AroundConstruct interceptor method must not be declared as a final method.",
                                                              DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnFinalMethod",
                                                              new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundConstruct")));

        Diagnostic invalidMulipleModifierStaticDiagnostics = d(21, 31, 50,
                                                               "AroundConstruct lifecycle callback interceptor method must not be declared as static except in an application client.",
                                                               DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnStaticMethod",
                                                               new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundConstruct")));

        // Test diagnostics for duplicate interceptor methods
        Diagnostic duplicateAroundConstruct2 = d(16, 25, 34,
                                                 "Only one method with @AroundConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                                 DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic duplicateAroundConstruct3 = d(21, 31, 50,
                                                 "Only one method with @AroundConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                                 DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic duplicateAroundConstruct4 = d(26, 18, 26,
                                                 "Only one method with @AroundConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                                 DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // InvalidInterceptorProceedNotInTryCatch warnings: logFinal (8,24,32), logStatic (16,25,34), logMulipleModifiers (21,31,50), logValid (26,18,26)
        Diagnostic proceedNotInTryCatchFinal = d(8, 24, 32,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchStatic = d(16, 25, 34,
                                                  PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                  DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchMultiple = d(21, 31, 50,
                                                    PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                    DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchValid = d(26, 18, 26,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, finalModifierDiagnostic, abstractModifierDiagnostic, duplicateAroundConstruct1,
                              proceedDiagnostics, staticModifierDiagnostic, invalidAbstractClassDiagnostics, invalidMulipleModifierFinalDiagnostics,
                              invalidMulipleModifierStaticDiagnostics, duplicateAroundConstruct2, duplicateAroundConstruct3, duplicateAroundConstruct4,
                              proceedNotInTryCatchFinal, proceedNotInTryCatchStatic, proceedNotInTryCatchMultiple, proceedNotInTryCatchValid);

        // Test code actions for final modifier
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, finalModifierDiagnostic);
        TextEdit removeAroundConstructOnFinalEdit = te(7, 1, 8, 4, "");
        TextEdit removeFinalEdit = te(8, 10, 8, 16, "");
        CodeAction removeAroundConstructOnFinalAction = ca(uri, "Remove @AroundConstruct", finalModifierDiagnostic, removeAroundConstructOnFinalEdit);
        CodeAction removeFinalAction = ca(uri, "Remove the 'final' modifier", finalModifierDiagnostic, removeFinalEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAroundConstructOnFinalAction, removeFinalAction);

        // Test code actions for abstract modifier
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, abstractModifierDiagnostic);
        TextEdit removeAroundConstructOnAbstractEdit = te(12, 4, 13, 4, "");
        TextEdit removeAbstractEdit = te(13, 10, 13, 19, "");
        CodeAction removeAroundConstructOnAbstractAction = ca(uri, "Remove @AroundConstruct", abstractModifierDiagnostic, removeAroundConstructOnAbstractEdit);
        CodeAction removeAbstractAction = ca(uri, "Remove the 'abstract' modifier", abstractModifierDiagnostic, removeAbstractEdit);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, removeAroundConstructOnAbstractAction, removeAbstractAction);

        // Test code actions for static modifier
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, staticModifierDiagnostic);
        TextEdit removeAroundConstructOnStaticEdit = te(15, 4, 16, 4, "");
        TextEdit removeStaticEdit = te(16, 10, 16, 17, "");
        CodeAction removeAroundConstructOnStaticAction = ca(uri, "Remove @AroundConstruct", staticModifierDiagnostic, removeAroundConstructOnStaticEdit);
        CodeAction removeStaticAction = ca(uri, "Remove the 'static' modifier", staticModifierDiagnostic, removeStaticEdit);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, removeAroundConstructOnStaticAction, removeStaticAction);

        // Test code actions for multiple modifiers
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, invalidMulipleModifierFinalDiagnostics);
        TextEdit removeAroundConstructOnFinalMultipleEdit = te(20, 4, 21, 4, "");
        TextEdit removeFinalMultipleEdit = te(21, 17, 21, 23, "");
        CodeAction removeAroundConstructOnFinalMultipleAction = ca(uri, "Remove @AroundConstruct", invalidMulipleModifierFinalDiagnostics,
                                                                   removeAroundConstructOnFinalMultipleEdit);
        CodeAction removeFinalMultipleAction = ca(uri, "Remove the 'final' modifier", invalidMulipleModifierFinalDiagnostics, removeFinalMultipleEdit);
        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, removeAroundConstructOnFinalMultipleAction, removeFinalMultipleAction);

        // Test code actions for multiple modifiers
        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, invalidMulipleModifierStaticDiagnostics);
        TextEdit removeAroundConstructOnStaticMultipleEdit = te(20, 4, 21, 4, "");
        TextEdit removeStaticMultipleEdit = te(21, 11, 21, 18, "");
        CodeAction removeAroundConstructOnStaticMultipleAction = ca(uri, "Remove @AroundConstruct", invalidMulipleModifierStaticDiagnostics,
                                                                    removeAroundConstructOnStaticMultipleEdit);
        CodeAction removeStaticMultipleAction = ca(uri, "Remove the 'static' modifier", invalidMulipleModifierStaticDiagnostics, removeStaticMultipleEdit);
        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, removeAroundConstructOnStaticMultipleAction, removeStaticMultipleAction);

    }

    @Test
    public void invalidAroundTimeoutMethodModifiersTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidAroundTimeoutMethods.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics for invalid method modifiers
        Diagnostic finalModifierDiagnostic = d(8, 24, 32,
                                               "AroundTimeout interceptor method must not be declared as a final method.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnFinalMethod",
                                               new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundTimeout")));

        Diagnostic abstractModifierDiagnostic = d(13, 27, 38,
                                                  "AroundTimeout interceptor method must not be declared as an abstract method.",
                                                  DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnAbstractMethod",
                                                  new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundTimeout")));

        Diagnostic duplicateAroundTimeout1 = d(13, 27, 38,
                                               "Only one method with @AroundTimeout annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic proceedDiagnostic = d(13, 27, 38,
                                         "Interceptor methods must always call the InvocationContext.proceed method.",
                                         DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");

        Diagnostic staticModifierDiagnostic = d(16, 25, 34,
                                                "AroundTimeout interceptor method must not be declared as a static method.",
                                                DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnStaticMethod",
                                                new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.AroundTimeout")));

        Diagnostic invalidAbstractClassDiagnostic = d(5, 22, 49,
                                                      "The class InvalidAroundTimeoutMethods should not contain the abstract modifier. If it contains the abstract modifier, the class should not be annotated with @Interceptor.",
                                                      DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorAnnotationOnAbstractClass");

        // Test diagnostics for duplicate interceptor methods
        Diagnostic duplicateAroundTimeout2 = d(16, 25, 34,
                                               "Only one method with @AroundTimeout annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic duplicateAroundTimeout3 = d(21, 18, 26,
                                               "Only one method with @AroundTimeout annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // InvalidInterceptorProceedNotInTryCatch warnings: logFinal (8,24,32), logStatic (16,25,34), logValid (21,18,26)
        Diagnostic proceedNotInTryCatchFinal = d(8, 24, 32,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchStatic = d(16, 25, 34,
                                                  PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                  DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchValid = d(21, 18, 26,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, finalModifierDiagnostic, abstractModifierDiagnostic, duplicateAroundTimeout1, proceedDiagnostic,
                              staticModifierDiagnostic, invalidAbstractClassDiagnostic, duplicateAroundTimeout2, duplicateAroundTimeout3,
                              proceedNotInTryCatchFinal, proceedNotInTryCatchStatic, proceedNotInTryCatchValid);

        // Test code actions for final modifier
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, finalModifierDiagnostic);
        TextEdit removeAroundTimeoutOnFinalEdit = te(7, 1, 8, 4, "");
        TextEdit removeFinalEdit = te(8, 10, 8, 16, "");
        CodeAction removeAroundTimeoutOnFinalAction = ca(uri, "Remove @AroundTimeout", finalModifierDiagnostic, removeAroundTimeoutOnFinalEdit);
        CodeAction removeFinalAction = ca(uri, "Remove the 'final' modifier", finalModifierDiagnostic, removeFinalEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAroundTimeoutOnFinalAction, removeFinalAction);

        // Test code actions for abstract modifier
        codeActionParams = createCodeActionParams(uri, abstractModifierDiagnostic);
        TextEdit removeAroundTimeoutOnAbstractEdit = te(12, 4, 13, 4, "");
        TextEdit removeAbstractEdit = te(13, 10, 13, 19, "");
        CodeAction removeAroundTimeoutOnAbstractAction = ca(uri, "Remove @AroundTimeout", abstractModifierDiagnostic, removeAroundTimeoutOnAbstractEdit);
        CodeAction removeAbstractAction = ca(uri, "Remove the 'abstract' modifier", abstractModifierDiagnostic, removeAbstractEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAroundTimeoutOnAbstractAction, removeAbstractAction);

        // Test code actions for static modifier
        codeActionParams = createCodeActionParams(uri, staticModifierDiagnostic);
        TextEdit removeAroundTimeoutOnStaticEdit = te(15, 4, 16, 4, "");
        TextEdit removeStaticEdit = te(16, 10, 16, 17, "");
        CodeAction removeAroundTimeoutOnStaticAction = ca(uri, "Remove @AroundTimeout", staticModifierDiagnostic, removeAroundTimeoutOnStaticEdit);
        CodeAction removeStaticAction = ca(uri, "Remove the 'static' modifier", staticModifierDiagnostic, removeStaticEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAroundTimeoutOnStaticAction, removeStaticAction);
    }

    @Test
    public void invalidPostConstructMethodModifiersTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidPostConstructMethods.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics for abstract class
        Diagnostic abstractClassDiagnostic = d(7, 22, 49,
                                               "The class InvalidPostConstructMethods should not contain the abstract modifier. If it contains the abstract modifier, the class should not be annotated with @Interceptor.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorAnnotationOnAbstractClass");

        // Test diagnostics for invalid method modifiers
        Diagnostic finalModifierDiagnostic = d(10, 24, 32,
                                               "PostConstruct interceptor method must not be declared as a final method.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnFinalMethod",
                                               new Gson().toJsonTree(Arrays.asList("jakarta.annotation.PostConstruct")));

        Diagnostic abstractModifierDiagnostic = d(15, 27, 38,
                                                  "PostConstruct interceptor method must not be declared as an abstract method.",
                                                  DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnAbstractMethod",
                                                  new Gson().toJsonTree(Arrays.asList("jakarta.annotation.PostConstruct")));

        Diagnostic duplicatePostConstruct1 = d(15, 27, 38,
                                               "Only one method with @PostConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic proceedDiagnostic = d(15, 27, 38,
                                         "Interceptor methods must always call the InvocationContext.proceed method.",
                                         DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");

        Diagnostic staticModifierDiagnostic = d(18, 25, 34,
                                                "PostConstruct lifecycle callback interceptor method must not be declared as static except in an application client.",
                                                DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnStaticMethod",
                                                new Gson().toJsonTree(Arrays.asList("jakarta.annotation.PostConstruct")));

        // Test diagnostics for duplicate interceptor methods
        Diagnostic duplicatePostConstruct2 = d(18, 25, 34,
                                               "Only one method with @PostConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic duplicatePostConstruct3 = d(23, 18, 26,
                                               "Only one method with @PostConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // InvalidInterceptorProceedNotInTryCatch warnings: logFinal (10,24,32), logStatic (18,25,34), logValid (23,18,26)
        Diagnostic proceedNotInTryCatchFinal = d(10, 24, 32,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchStatic = d(18, 25, 34,
                                                  PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                  DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchValid = d(23, 18, 26,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, abstractClassDiagnostic, finalModifierDiagnostic,
                              abstractModifierDiagnostic, duplicatePostConstruct1, proceedDiagnostic, staticModifierDiagnostic,
                              duplicatePostConstruct2, duplicatePostConstruct3,
                              proceedNotInTryCatchFinal, proceedNotInTryCatchStatic, proceedNotInTryCatchValid);

        // Test code actions for final modifier
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, finalModifierDiagnostic);
        TextEdit removePostConstructOnFinalEdit = te(9, 1, 10, 4, "");
        TextEdit removeFinalEdit = te(10, 10, 10, 16, "");
        CodeAction removePostConstructOnFinalAction = ca(uri, "Remove @PostConstruct", finalModifierDiagnostic, removePostConstructOnFinalEdit);
        CodeAction removeFinalAction = ca(uri, "Remove the 'final' modifier", finalModifierDiagnostic, removeFinalEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removePostConstructOnFinalAction, removeFinalAction);

        // Test code actions for abstract modifier
        codeActionParams = createCodeActionParams(uri, abstractModifierDiagnostic);
        TextEdit removePostConstructOnAbstractEdit = te(14, 1, 15, 4, "");
        TextEdit removeAbstractEdit = te(15, 10, 15, 19, "");
        CodeAction removePostConstructOnAbstractAction = ca(uri, "Remove @PostConstruct", abstractModifierDiagnostic, removePostConstructOnAbstractEdit);
        CodeAction removeAbstractAction = ca(uri, "Remove the 'abstract' modifier", abstractModifierDiagnostic, removeAbstractEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removePostConstructOnAbstractAction, removeAbstractAction);

        // Test code actions for static modifier
        codeActionParams = createCodeActionParams(uri, staticModifierDiagnostic);
        TextEdit removePostConstructOnStaticEdit = te(17, 1, 18, 4, "");
        TextEdit removeStaticEdit = te(18, 10, 18, 17, "");
        CodeAction removePostConstructOnStaticAction = ca(uri, "Remove @PostConstruct", staticModifierDiagnostic, removePostConstructOnStaticEdit);
        CodeAction removeStaticAction = ca(uri, "Remove the 'static' modifier", staticModifierDiagnostic, removeStaticEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removePostConstructOnStaticAction, removeStaticAction);
    }

    @Test
    public void invalidPreDestroyMethodModifiersTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidPreDestroyMethods.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics for abstract class
        Diagnostic abstractClassDiagnostic = d(7, 22, 46,
                                               "The class InvalidPreDestroyMethods should not contain the abstract modifier. If it contains the abstract modifier, the class should not be annotated with @Interceptor.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorAnnotationOnAbstractClass");

        // Test diagnostics for invalid method modifiers
        Diagnostic finalModifierDiagnostic = d(10, 24, 32,
                                               "PreDestroy interceptor method must not be declared as a final method.",
                                               DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnFinalMethod",
                                               new Gson().toJsonTree(Arrays.asList("jakarta.annotation.PreDestroy")));

        Diagnostic abstractModifierDiagnostic = d(15, 27, 38,
                                                  "PreDestroy interceptor method must not be declared as an abstract method.",
                                                  DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnAbstractMethod",
                                                  new Gson().toJsonTree(Arrays.asList("jakarta.annotation.PreDestroy")));

        Diagnostic duplicatePreDestroy1 = d(15, 27, 38,
                                            "Only one method with @PreDestroy annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                            DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic proceedDiagnostic = d(15, 27, 38,
                                         "Interceptor methods must always call the InvocationContext.proceed method.",
                                         DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorMethodsProceedMissing");

        Diagnostic staticModifierDiagnostic = d(18, 25, 34,
                                                "PreDestroy lifecycle callback interceptor method must not be declared as static except in an application client.",
                                                DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorMethodAnnotationOnStaticMethod",
                                                new Gson().toJsonTree(Arrays.asList("jakarta.annotation.PreDestroy")));

        // Test diagnostics for duplicate interceptor methods
        Diagnostic duplicatePreDestroy2 = d(18, 25, 34,
                                            "Only one method with @PreDestroy annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                            DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic duplicatePreDestroy3 = d(23, 18, 26,
                                            "Only one method with @PreDestroy annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                            DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // InvalidInterceptorProceedNotInTryCatch warnings: logFinal (10,24,32), logStatic (18,25,34), logValid (23,18,26)
        Diagnostic proceedNotInTryCatchFinal = d(10, 24, 32,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchStatic = d(18, 25, 34,
                                                  PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                  DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic proceedNotInTryCatchValid = d(23, 18, 26,
                                                 PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                 DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, abstractClassDiagnostic, finalModifierDiagnostic,
                              abstractModifierDiagnostic, duplicatePreDestroy1, proceedDiagnostic, staticModifierDiagnostic,
                              duplicatePreDestroy2, duplicatePreDestroy3,
                              proceedNotInTryCatchFinal, proceedNotInTryCatchStatic, proceedNotInTryCatchValid);

        // Test code actions for final modifier
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, finalModifierDiagnostic);
        TextEdit removePreDestroyOnFinalEdit = te(9, 1, 10, 4, "");
        TextEdit removeFinalEdit = te(10, 10, 10, 16, "");
        CodeAction removePreDestroyOnFinalAction = ca(uri, "Remove @PreDestroy", finalModifierDiagnostic, removePreDestroyOnFinalEdit);
        CodeAction removeFinalAction = ca(uri, "Remove the 'final' modifier", finalModifierDiagnostic, removeFinalEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removePreDestroyOnFinalAction, removeFinalAction);

        // Test code actions for abstract modifier
        codeActionParams = createCodeActionParams(uri, abstractModifierDiagnostic);
        TextEdit removePreDestroyOnAbstractEdit = te(14, 1, 15, 4, "");
        TextEdit removeAbstractEdit = te(15, 10, 15, 19, "");
        CodeAction removePreDestroyOnAbstractAction = ca(uri, "Remove @PreDestroy", abstractModifierDiagnostic, removePreDestroyOnAbstractEdit);
        CodeAction removeAbstractAction = ca(uri, "Remove the 'abstract' modifier", abstractModifierDiagnostic, removeAbstractEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removePreDestroyOnAbstractAction, removeAbstractAction);

        // Test code actions for static modifier
        codeActionParams = createCodeActionParams(uri, staticModifierDiagnostic);
        TextEdit removePreDestroyOnStaticEdit = te(17, 1, 18, 4, "");
        TextEdit removeStaticEdit = te(18, 10, 18, 17, "");
        CodeAction removePreDestroyOnStaticAction = ca(uri, "Remove @PreDestroy", staticModifierDiagnostic, removePreDestroyOnStaticEdit);
        CodeAction removeStaticAction = ca(uri, "Remove the 'static' modifier", staticModifierDiagnostic, removeStaticEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removePreDestroyOnStaticAction, removeStaticAction);
    }

    @Test
    public void invalidNegativePriorityInterceptorTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidNegativePriorityInterceptor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics - there are two diagnostics on the @Priority annotation:
        // 1. A warning from jakarta-annotations about negative priority values
        // 2. An error from jakarta-interceptor about negative priority values (our implementation)
        Diagnostic negativePriorityWarningAnnotation = d(7, 0, 15,
                                                         "Priority values should generally be non-negative, with negative values reserved for special meanings such as \"undefined\" or \"not specified\".",
                                                         DiagnosticSeverity.Warning, "jakarta-annotations", "PriorityShouldBeNonNegative");

        Diagnostic negativePriorityErrorInterceptor = d(7, 0, 15,
                                                        "Interceptor priority values must not be negative. Negative values are reserved for future use by the specification.",
                                                        DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidInterceptorNegativePriority");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, negativePriorityWarningAnnotation, negativePriorityErrorInterceptor);
    }

    @Test
    public void testMultipleInterceptorMethodsOfSameType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/MultipleInterceptorMethodsOfSameType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics for multiple @AroundInvoke methods
        Diagnostic aroundInvokeDuplicate1 = d(33, 18, 22,
                                              "Only one method with @AroundInvoke annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                              DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        Diagnostic aroundInvokeDuplicate2 = d(38, 18, 22,
                                              "Only one method with @AroundInvoke annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                              DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // Test diagnostics for multiple @AroundTimeout methods
        Diagnostic aroundTimeoutDuplicate = d(49, 18, 26,
                                              "Only one method with @AroundTimeout annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                              DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // Test diagnostics for multiple @PostConstruct methods
        Diagnostic postConstructDuplicate = d(60, 16, 21,
                                              "Only one method with @PostConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                              DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // Test diagnostics for multiple @PreDestroy methods
        Diagnostic preDestroyDuplicate = d(71, 16, 24,
                                           "Only one method with @PreDestroy annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                           DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // Test diagnostics for multiple @AroundConstruct methods
        Diagnostic aroundConstructDuplicate = d(82, 16, 26,
                                                "Only one method with @AroundConstruct annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                                DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // Test diagnostics for nested class with multiple @AroundInvoke methods
        Diagnostic nestedAroundInvokeDuplicate = d(96, 22, 29,
                                                   "Only one method with @AroundInvoke annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                                   DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        // InvalidInterceptorProceedNotInTryCatch warnings — every method with unwrapped proceed()
        Diagnostic log1UnwrappedProceed = d(28, 18, 22, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic log2UnwrappedProceed = d(33, 18, 22, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic log3UnwrappedProceed = d(38, 18, 22, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic timeout1UnwrappedProceed = d(44, 18, 26, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor",
                                                "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic timeout2UnwrappedProceed = d(49, 18, 26, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor",
                                                "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic init1UnwrappedProceed = d(55, 16, 21, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic init2UnwrappedProceed = d(60, 16, 21, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic destroy1UnwrappedProceed = d(66, 16, 24, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor",
                                                "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic destroy2UnwrappedProceed = d(71, 16, 24, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor",
                                                "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic construct1UnwrappedProceed = d(77, 16, 26, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor",
                                                  "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic construct2UnwrappedProceed = d(82, 16, 26, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor",
                                                  "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic nested1UnwrappedProceed = d(91, 22, 29, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor",
                                               "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic nested2UnwrappedProceed = d(96, 22, 29, PROCEED_NOT_IN_TRY_CATCH_MSG, DiagnosticSeverity.Warning, "jakarta-interceptor",
                                               "InvalidInterceptorProceedNotInTryCatch");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, aroundInvokeDuplicate1, aroundInvokeDuplicate2,
                              aroundTimeoutDuplicate, postConstructDuplicate, preDestroyDuplicate,
                              aroundConstructDuplicate, nestedAroundInvokeDuplicate,
                              log1UnwrappedProceed, log2UnwrappedProceed, log3UnwrappedProceed,
                              timeout1UnwrappedProceed, timeout2UnwrappedProceed,
                              init1UnwrappedProceed, init2UnwrappedProceed,
                              destroy1UnwrappedProceed, destroy2UnwrappedProceed,
                              construct1UnwrappedProceed, construct2UnwrappedProceed,
                              nested1UnwrappedProceed, nested2UnwrappedProceed);
    }

    @Test
    public void testValidInterceptorOneMethodPerType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/ValidInterceptorOneMethodPerType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Assert NO diagnostics for valid code with one method per type
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testInterceptorMissingBindingAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidInterceptorMissingBinding.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for interceptor without binding annotation
        // Line 14: public class InvalidInterceptorMissingBinding {
        // Position: "InvalidInterceptorMissingBinding" starts at column 13 (0-based)
        Diagnostic missingBindingDiagnostic = d(13, 13, 45,
                                                "An interceptor declared using @Interceptor must specify at least one interceptor binding annotation.",
                                                DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorMissingInterceptorBinding");

        Diagnostic proceedNotInTryCatch = d(16, 18, 21,
                                            PROCEED_NOT_IN_TRY_CATCH_MSG,
                                            DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, missingBindingDiagnostic, proceedNotInTryCatch);
    }

    @Test
    public void testInterceptorWithValidBindingAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/ValidInterceptorWithBinding.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test that valid interceptor with @Monitored binding does NOT trigger diagnostic
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testInterceptorProceedNotInTryCatchTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/InvalidInterceptorProceedNotInTryCatch.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic aroundInvokeWarning = d(16, 18, 30,
                                           PROCEED_NOT_IN_TRY_CATCH_MSG,
                                           DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic aroundConstructWarning = d(21, 18, 33,
                                              PROCEED_NOT_IN_TRY_CATCH_MSG,
                                              DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic aroundTimeoutWarning = d(26, 18, 31,
                                            PROCEED_NOT_IN_TRY_CATCH_MSG,
                                            DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic postConstructWarning = d(31, 16, 29,
                                            PROCEED_NOT_IN_TRY_CATCH_MSG,
                                            DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        Diagnostic preDestroyWarning = d(36, 16, 26,
                                         PROCEED_NOT_IN_TRY_CATCH_MSG,
                                         DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");

        // Corner case: proceed() called outside the try block — should still warn
        Diagnostic proceedOutsideTryWarning = d(42, 18, 35,
                                                PROCEED_NOT_IN_TRY_CATCH_MSG,
                                                DiagnosticSeverity.Warning, "jakarta-interceptor", "InvalidInterceptorProceedNotInTryCatch");
        // proceedOutsideTry is a second @AroundInvoke method — duplicate annotation diagnostic also fires
        Diagnostic duplicateAroundInvoke = d(42, 18, 35,
                                             "Only one method with @AroundInvoke annotation is allowed per class. Multiple methods with the same interceptor annotation type are not permitted.",
                                             DiagnosticSeverity.Error, "jakarta-interceptor", "InvalidMultipleInterceptorMethodsOfSameType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, aroundInvokeWarning, aroundConstructWarning,
                              aroundTimeoutWarning, postConstructWarning, preDestroyWarning, duplicateAroundInvoke,
                              proceedOutsideTryWarning);
    }

    @Test
    public void testValidInterceptorProceedInTryCatchTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/interceptor/ValidInterceptorProceedInTryCatch.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // All interceptor methods use proceed() inside try/catch/finally — no warnings expected
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
