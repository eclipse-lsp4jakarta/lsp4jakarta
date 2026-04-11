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
import org.junit.Test;

import com.google.gson.Gson;

public class ManagedBeanTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void managedBeanAnnotations() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ManagedBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test expected diagnostic
        Diagnostic d1 = d(9, 12, 13,
                          "The @Dependent annotation must be the only scope defined by a managed bean with a non-static public field.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidManagedBeanWithNonStaticPublicField");
        Diagnostic d2 = d(6, 12, 13,
                          "The @Dependent annotation must be the only scope defined by a managed bean with a non-static public field.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidManagedBeanWithNonStaticPublicField");
        Diagnostic d3 = d(5, 13, 24,
                          "The @Dependent annotation must be the only scope defined by a Managed bean class of generic type.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidGenericManagedBeanClassWithNoDependentScope");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);

        // Assert for diagnostic d2
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d2);
        TextEdit te2 = te(4, 0, 5, 0, "@Dependent\n");
        CodeAction ca2 = ca(uri, "Replace current scope with @Dependent", d2, te2);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca2);

        // Assert for diagnostic d3
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d3);
        TextEdit te3 = te(4, 0, 5, 0, "@Dependent\n");
        CodeAction ca3 = ca(uri, "Replace current scope with @Dependent", d3, te3);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3);
    }

    @Test
    public void ManagedBeanWithDependent() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ManagedBeanWithDependent.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test expected diagnostic
        Diagnostic d1 = d(37, 6, 36,
                          "Scope type annotations must be specified by a managed bean class at most once.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        d1.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped", "jakarta.enterprise.context.RequestScoped")));

        Diagnostic d2 = d(27, 6, 33,
                          "The @Dependent annotation must be the only scope defined by a Managed bean class of generic type.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidGenericManagedBeanClassWithNoDependentScope");
        Diagnostic d3 = d(18, 12, 13,
                          "The @Dependent annotation must be the only scope defined by a managed bean with a non-static public field.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidManagedBeanWithNonStaticPublicField");

        Diagnostic d4 = d(17, 6, 27,
                          "The @Dependent annotation must be the only scope defined by a managed bean with a non-static public field.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidManagedBeanWithNonStaticPublicField");
        Diagnostic d5 = d(7, 12, 13,
                          "The @Dependent annotation must be the only scope defined by a managed bean with a non-static public field.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidManagedBeanWithNonStaticPublicField");

        Diagnostic d6 = d(6, 13, 37,
                          "The @Dependent annotation must be the only scope defined by a Managed bean class of generic type.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidGenericManagedBeanClassWithNoDependentScope");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6);

        // Assert for diagnostic d1
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te11 = te(35, 0, 36, 0, "");
        CodeAction ca11 = ca(uri, "Remove @RequestScoped", d1, te11);
        TextEdit te12 = te(35, 14, 36, 14, "");
        CodeAction ca12 = ca(uri, "Remove @SessionScoped", d1, te12);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca11, ca12);

        // Assert for diagnostic d2
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te2 = te(25, 0, 26, 14, "@Dependent");
        CodeAction ca2 = ca(uri, "Replace current scope with @Dependent", d2, te2);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca2);

        // Assert for diagnostic d3
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);
        TextEdit te3 = te(14, 0, 16, 14, "@Dependent");
        CodeAction ca3 = ca(uri, "Replace current scope with @Dependent", d3, te3);
        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca3);

        // Assert for diagnostic d4
        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, d4);
        TextEdit te4 = te(14, 0, 16, 14, "@Dependent");
        CodeAction ca4 = ca(uri, "Replace current scope with @Dependent", d4, te4);
        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, ca4);

        // Assert for diagnostic d5
        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, d5);
        TextEdit te5 = te(4, 0, 6, 0, "@Dependent\n");
        CodeAction ca5 = ca(uri, "Replace current scope with @Dependent", d5, te5);
        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, ca5);

        // Assert for diagnostic d6
        JakartaJavaCodeActionParams codeActionParams6 = createCodeActionParams(uri, d6);
        TextEdit te6 = te(4, 0, 6, 0, "@Dependent\n");
        CodeAction ca6 = ca(uri, "Replace current scope with @Dependent", d6, te6);
        assertJavaCodeAction(codeActionParams6, IJDT_UTILS, ca6);
    }

    @Test
    public void scopeDeclaration() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ScopeDeclaration.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // test expected diagnostic
        Diagnostic d1 = d(12, 16, 17,
                          "Scope type annotations must be specified by a producer field at most once.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopeAnnotationsByProducerField");
        d1.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent", "jakarta.enterprise.context.ApplicationScoped",
                                                       "jakarta.enterprise.inject.Produces")));

        Diagnostic d2 = d(15, 25, 41, "Scope type annotations must be specified by a producer method at most once.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopeAnnotationsByProducerMethod");
        d2.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped", "jakarta.enterprise.context.RequestScoped",
                                                       "jakarta.enterprise.inject.Produces")));

        Diagnostic d3 = d(10, 13, 29, "Scope type annotations must be specified by a managed bean class at most once.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        d3.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped", "jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);

        // Assert for the diagnostic d1
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1 = te(11, 33, 12, 4, "");
        TextEdit te2 = te(11, 14, 11, 33, "");
        CodeAction ca1 = ca(uri, "Remove @ApplicationScoped", d1, te2);
        CodeAction ca2 = ca(uri, "Remove @Dependent", d1, te1);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1, ca2);

        // Assert for the diagnostic d2
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te3 = te(14, 33, 15, 4, "");
        TextEdit te4 = te(14, 14, 14, 33, "");
        CodeAction ca3 = ca(uri, "Remove @ApplicationScoped", d2, te4);
        CodeAction ca4 = ca(uri, "Remove @RequestScoped", d2, te3);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3, ca4);

        // Assert for the diagnostic d3
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);
        TextEdit te5 = te(9, 19, 10, 0, "");
        TextEdit te6 = te(9, 0, 9, 19, "");
        CodeAction ca5 = ca(uri, "Remove @ApplicationScoped", d3, te6);
        CodeAction ca6 = ca(uri, "Remove @RequestScoped", d3, te5);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca5, ca6);
    }

    @Test
    public void producesAndInject() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ProducesAndInjectTogether.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d1 = d(16, 18, 23,
                          "The @Produces and @Inject annotations must not be used on the same method.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMethodWithProducesAndInjectAnnotations");

        Diagnostic d2 = d(11, 19, 27,
                          "The @Produces and @Inject annotations must not be used on the same field or property.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidFieldWithProducesAndInjectAnnotations");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);

        TextEdit te1 = te(14, 4, 15, 4, "");
        TextEdit te2 = te(15, 4, 16, 4, "");
        CodeAction ca1 = ca(uri, "Remove @Inject", d1, te2);
        CodeAction ca2 = ca(uri, "Remove @Produces", d1, te1);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1, ca2);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);

        TextEdit te3 = te(9, 4, 10, 4, "");
        TextEdit te4 = te(10, 4, 11, 4, "");
        CodeAction ca3 = ca(uri, "Remove @Inject", d2, te4);
        CodeAction ca4 = ca(uri, "Remove @Produces", d2, te3);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3, ca4);
    }

    @Test
    public void injectAndDisposesObservesObservesAsync() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/InjectAndDisposesObservesObservesAsync.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic injectWithDisposes = d(10, 18, 31,
                                          "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes.",
                                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInjectAnnotatedMethodParamAnnotation");

        Diagnostic injectWithObserves = d(16, 18, 31,
                                          "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Observes.",
                                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInjectAnnotatedMethodParamAnnotation");

        Diagnostic injectWithObservesAsync = d(22, 18, 36,
                                               "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @ObservesAsync.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInjectAnnotatedMethodParamAnnotation");

        Diagnostic injectWithDisposesAndObserves = d(28, 18, 39,
                                                     "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes, @Observes.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInjectAnnotatedMethodParamAnnotation");

        Diagnostic multipleObserverParams1 = d(34, 18, 44,
                                               "Parameters name1, name2 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        Diagnostic injectWithObservesAndObservesAsync = d(34, 18, 44,
                                                          "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Observes, @ObservesAsync.",
                                                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInjectAnnotatedMethodParamAnnotation");

        Diagnostic injectWithDisposesAndObservesAsync = d(40, 18, 44,
                                                          "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes, @ObservesAsync.",
                                                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInjectAnnotatedMethodParamAnnotation");

        Diagnostic multipleObserverParams2 = d(46, 18, 52,
                                               "Parameters name2, name3 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        Diagnostic injectWithAllThreeAnnotations = d(46, 18, 52,
                                                     "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes, @Observes, @ObservesAsync.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInjectAnnotatedMethodParamAnnotation");

        Diagnostic observesAndObservesAsyncOnSameParam = d(51, 18, 53,
                                                           "A CDI method must not have parameter(s): name annotated with @Observes and @ObservesAsync.",
                                                           DiagnosticSeverity.Error, "jakarta-cdi", "InvalidObservesObservesAsyncMethodParams");

        Diagnostic injectWithAllAnnotationsOnSameParam = d(51, 18, 53,
                                                           "A bean constructor or a method annotated with @Inject cannot have parameter(s) annotated with @Disposes, @Observes, @ObservesAsync.",
                                                           DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInjectAnnotationOnMultipleMethodParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, injectWithDisposes, injectWithObserves, injectWithObservesAsync,
                              injectWithDisposesAndObserves, multipleObserverParams1, injectWithObservesAndObservesAsync,
                              injectWithDisposesAndObservesAsync, multipleObserverParams2, injectWithAllThreeAnnotations,
                              observesAndObservesAsyncOnSameParam, injectWithAllAnnotationsOnSameParam);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, injectWithDisposes);

        TextEdit removeInject1 = te(9, 4, 10, 4, "");
        TextEdit removeDisposesParam1 = te(10, 32, 10, 68, "");
        CodeAction removeInjectAction1 = ca(uri, "Remove @Inject", injectWithDisposes, removeInject1);
        CodeAction removeDisposesParamAction1 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name'", injectWithDisposes, removeDisposesParam1);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, removeInjectAction1, removeDisposesParamAction1);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, injectWithObserves);

        TextEdit removeInject2 = te(15, 4, 16, 4, "");
        TextEdit removeObservesParam2 = te(16, 32, 16, 42, "");
        CodeAction removeInjectAction2 = ca(uri, "Remove @Inject", injectWithObserves, removeInject2);
        CodeAction removeObservesParamAction2 = ca(uri, "Remove the '@Observes' modifier from parameter 'name'", injectWithObserves, removeObservesParam2);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, removeInjectAction2, removeObservesParamAction2);

        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, injectWithObservesAsync);

        TextEdit removeInject3 = te(21, 4, 22, 4, "");
        TextEdit removeObservesAsyncParam3 = te(22, 37, 22, 52, "");
        CodeAction removeInjectAction3 = ca(uri, "Remove @Inject", injectWithObservesAsync, removeInject3);
        CodeAction removeObservesAsyncParamAction3 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name'", injectWithObservesAsync, removeObservesAsyncParam3);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, removeInjectAction3, removeObservesAsyncParamAction3);

        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, injectWithDisposesAndObserves);

        TextEdit removeInject4 = te(27, 4, 28, 4, "");
        TextEdit removeDisposesParam4 = te(28, 40, 28, 50, "");
        TextEdit removeObservesParam4 = te(28, 64, 28, 74, "");
        CodeAction removeInjectAction4 = ca(uri, "Remove @Inject", injectWithDisposesAndObserves, removeInject4);
        CodeAction removeDisposesParamAction4 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", injectWithDisposesAndObserves, removeDisposesParam4);
        CodeAction removeObservesParamAction4 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", injectWithDisposesAndObserves, removeObservesParam4);

        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, removeInjectAction4, removeDisposesParamAction4, removeObservesParamAction4);

        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, injectWithObservesAndObservesAsync);

        TextEdit removeInject5 = te(33, 4, 34, 4, "");
        TextEdit removeObservesParam5 = te(34, 45, 34, 55, "");
        TextEdit removeObservesAsyncParam5 = te(34, 69, 34, 109, "");
        CodeAction removeInjectAction5 = ca(uri, "Remove @Inject", injectWithObservesAndObservesAsync, removeInject5);
        CodeAction removeObservesParamAction5 = ca(uri, "Remove the '@Observes' modifier from parameter 'name1'", injectWithObservesAndObservesAsync, removeObservesParam5);
        CodeAction removeObservesAsyncParamAction5 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", injectWithObservesAndObservesAsync,
                                                        removeObservesAsyncParam5);

        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, removeInjectAction5, removeObservesParamAction5, removeObservesAsyncParamAction5);

        JakartaJavaCodeActionParams codeActionParams6 = createCodeActionParams(uri, injectWithDisposesAndObservesAsync);

        TextEdit removeInject6 = te(39, 4, 40, 4, "");
        TextEdit removeDisposesParam6 = te(40, 45, 40, 55, "");
        TextEdit removeObservesAsyncParam6 = te(40, 69, 40, 84, "");
        CodeAction removeInjectAction6 = ca(uri, "Remove @Inject", injectWithDisposesAndObservesAsync, removeInject6);
        CodeAction removeDisposesParamAction6 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", injectWithDisposesAndObservesAsync, removeDisposesParam6);
        CodeAction removeObservesAsyncParamAction6 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", injectWithDisposesAndObservesAsync,
                                                        removeObservesAsyncParam6);

        assertJavaCodeAction(codeActionParams6, IJDT_UTILS, removeInjectAction6, removeDisposesParamAction6, removeObservesAsyncParamAction6);

        JakartaJavaCodeActionParams codeActionParams7 = createCodeActionParams(uri, injectWithAllThreeAnnotations);

        TextEdit removeInject7 = te(45, 4, 46, 4, "");
        TextEdit removeDisposesParam7 = te(46, 53, 46, 63, "");
        TextEdit removeObservesParam7 = te(46, 77, 46, 87, "");
        TextEdit removeObservesAsyncParam7 = te(46, 101, 46, 116, "");
        CodeAction removeInjectAction7 = ca(uri, "Remove @Inject", injectWithAllThreeAnnotations, removeInject7);
        CodeAction removeDisposesParamAction7 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", injectWithAllThreeAnnotations, removeDisposesParam7);
        CodeAction removeObservesParamAction7 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", injectWithAllThreeAnnotations, removeObservesParam7);
        CodeAction removeObservesAsyncParamAction7 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name3'", injectWithAllThreeAnnotations,
                                                        removeObservesAsyncParam7);

        assertJavaCodeAction(codeActionParams7, IJDT_UTILS, removeInjectAction7, removeDisposesParamAction7, removeObservesParamAction7, removeObservesAsyncParamAction7);

        JakartaJavaCodeActionParams codeActionParams8 = createCodeActionParams(uri, injectWithAllAnnotationsOnSameParam);

        TextEdit removeInject8 = te(50, 4, 51, 4, "");
        TextEdit removeDisposesParam8 = te(51, 54, 51, 64, "");
        TextEdit removeAllThreeParams8 = te(51, 54, 51, 89, "");
        TextEdit removeObservesAndObservesAsyncParams8 = te(51, 63, 51, 88, "");
        CodeAction removeInjectAction8 = ca(uri, "Remove @Inject", injectWithAllAnnotationsOnSameParam, removeInject8);
        CodeAction removeDisposesParamAction8 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name'", injectWithAllAnnotationsOnSameParam, removeDisposesParam8);
        CodeAction removeAllThreeParamsAction8 = ca(uri, "Remove the '@Disposes', '@Observes', '@ObservesAsync' modifier from parameter 'name'",
                                                    injectWithAllAnnotationsOnSameParam, removeAllThreeParams8);
        CodeAction removeObservesAndObservesAsyncParamsAction8 = ca(uri, "Remove the '@Observes', '@ObservesAsync' modifier from parameter 'name'",
                                                                    injectWithAllAnnotationsOnSameParam, removeObservesAndObservesAsyncParams8);

        assertJavaCodeAction(codeActionParams8, IJDT_UTILS, removeInjectAction8, removeDisposesParamAction8, removeAllThreeParamsAction8,
                             removeObservesAndObservesAsyncParamsAction8);

    }

    @Test
    public void producesAndDisposesObservesObservesAsync() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ProducesAndDisposesObservesObservesAsync.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic producesWithDisposes = d(12, 18, 31,
                                            "A producer method cannot have parameter(s) annotated with @Disposes.",
                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerMethodParamAnnotation");

        Diagnostic producesWithObserves = d(18, 18, 31,
                                            "A producer method cannot have parameter(s) annotated with @Observes.",
                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerMethodParamAnnotation");

        Diagnostic producesWithObservesAsync = d(24, 18, 36,
                                                 "A producer method cannot have parameter(s) annotated with @ObservesAsync.",
                                                 DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerMethodParamAnnotation");

        Diagnostic producesWithDisposesAndObserves = d(30, 18, 39,
                                                       "A producer method cannot have parameter(s) annotated with @Disposes, @Observes.",
                                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerMethodParamAnnotation");

        Diagnostic producesWithObservesAndObservesAsync = d(36, 18, 44,
                                                            "A producer method cannot have parameter(s) annotated with @Observes, @ObservesAsync.",
                                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerMethodParamAnnotation");

        Diagnostic producesWithDisposesAndObservesAsync = d(42, 18, 44,
                                                            "A producer method cannot have parameter(s) annotated with @Disposes, @ObservesAsync.",
                                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerMethodParamAnnotation");

        Diagnostic producesWithAllThreeAnnotations = d(48, 18, 52,
                                                       "A producer method cannot have parameter(s) annotated with @Disposes, @Observes, @ObservesAsync.",
                                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerMethodParamAnnotation");

        Diagnostic producesObservesAndObservesAsyncOnSameParam = d(54, 18, 53,
                                                                   "A CDI method must not have parameter(s): name annotated with @Observes and @ObservesAsync.",
                                                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidObservesObservesAsyncMethodParams");

        Diagnostic producesWithAllAnnotationsOnSameParam = d(54, 18, 53,
                                                             "A producer method cannot have parameter(s) annotated with @Disposes, @Observes, @ObservesAsync.",
                                                             DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerMethodParamAnnotation");

        Diagnostic disposerWithObserves = d(30, 18, 39,
                                            "A disposer method cannot have parameter(s) annotated with @Observes.",
                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidDisposerMethodParamAnnotation");

        Diagnostic disposerWithObservesAsync = d(42, 18, 44,
                                                 "A disposer method cannot have parameter(s) annotated with @ObservesAsync.",
                                                 DiagnosticSeverity.Error, "jakarta-cdi", "InvalidDisposerMethodParamAnnotation");

        Diagnostic multipleObserverParams1 = d(36, 18, 44,
                                               "Parameters name1, name2 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        Diagnostic multipleObserverParams2 = d(48, 18, 52,
                                               "Parameters name2, name3 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        Diagnostic disposerWithObservesAndObservesAsync = d(48, 18, 52,
                                                            "A disposer method cannot have parameter(s) annotated with @Observes, @ObservesAsync.",
                                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidDisposerMethodParamAnnotation");

        Diagnostic disposerWithAllAnnotationsOnSameParam = d(54, 18, 53,
                                                             "A disposer method cannot have parameter(s) annotated with @Observes, @ObservesAsync.",
                                                             DiagnosticSeverity.Error, "jakarta-cdi", "InvalidDisposerMethodParamAnnotation");

        Diagnostic observesAndObservesAsyncOnMultipleParams = d(58, 18, 52,
                                                                "A CDI method must not have parameter(s): name, name1 annotated with @Observes and @ObservesAsync.",
                                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidObservesObservesAsyncMethodParams");

        Diagnostic multipleObserverParams3 = d(58, 18, 52,
                                               "Parameters name, name1 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, producesWithDisposes, producesWithObserves, producesWithObservesAsync,
                              producesWithDisposesAndObserves, disposerWithObserves, multipleObserverParams1, producesWithObservesAndObservesAsync,
                              producesWithDisposesAndObservesAsync, disposerWithObservesAsync, multipleObserverParams2, producesWithAllThreeAnnotations,
                              disposerWithObservesAndObservesAsync, producesObservesAndObservesAsyncOnSameParam, producesWithAllAnnotationsOnSameParam,
                              disposerWithAllAnnotationsOnSameParam, observesAndObservesAsyncOnMultipleParams, multipleObserverParams3);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, producesWithDisposes);

        TextEdit removeProduces1 = te(11, 4, 12, 4, "");
        TextEdit removeDisposesParam1 = te(12, 32, 12, 42, "");
        CodeAction removeProducesAction1 = ca(uri, "Remove @Produces", producesWithDisposes, removeProduces1);
        CodeAction removeDisposesParamAction1 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name'", producesWithDisposes, removeDisposesParam1);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, removeProducesAction1, removeDisposesParamAction1);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, producesWithObserves);

        TextEdit removeProduces2 = te(17, 4, 18, 4, "");
        TextEdit removeObservesParam2 = te(18, 32, 18, 42, "");
        CodeAction removeProducesAction2 = ca(uri, "Remove @Produces", producesWithObserves, removeProduces2);
        CodeAction removeObservesParamAction2 = ca(uri, "Remove the '@Observes' modifier from parameter 'name'", producesWithObserves, removeObservesParam2);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, removeProducesAction2, removeObservesParamAction2);

        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, producesWithObservesAsync);

        TextEdit removeProduces3 = te(23, 4, 24, 4, "");
        TextEdit removeObservesAsyncParam3 = te(24, 37, 24, 52, "");
        CodeAction removeProducesAction3 = ca(uri, "Remove @Produces", producesWithObservesAsync, removeProduces3);
        CodeAction removeObservesAsyncParamAction3 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name'", producesWithObservesAsync, removeObservesAsyncParam3);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, removeProducesAction3, removeObservesAsyncParamAction3);

        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, producesWithDisposesAndObserves);

        TextEdit removeProduces4 = te(29, 4, 30, 4, "");
        TextEdit removeDisposesParam4 = te(30, 40, 30, 50, "");
        TextEdit removeObservesParam4 = te(30, 64, 30, 74, "");
        CodeAction removeProducesAction4 = ca(uri, "Remove @Produces", producesWithDisposesAndObserves, removeProduces4);
        CodeAction removeDisposesParamAction4 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", producesWithDisposesAndObserves, removeDisposesParam4);
        CodeAction removeObservesParamAction4 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", producesWithDisposesAndObserves, removeObservesParam4);

        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, removeProducesAction4, removeDisposesParamAction4, removeObservesParamAction4);

        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, producesWithObservesAndObservesAsync);

        TextEdit removeProduces5 = te(35, 4, 36, 4, "");
        TextEdit removeObservesParam5 = te(36, 45, 36, 55, "");
        TextEdit removeObservesAsyncParam5 = te(36, 69, 36, 84, "");
        CodeAction removeProducesAction5 = ca(uri, "Remove @Produces", producesWithObservesAndObservesAsync, removeProduces5);
        CodeAction removeObservesParamAction5 = ca(uri, "Remove the '@Observes' modifier from parameter 'name1'", producesWithObservesAndObservesAsync, removeObservesParam5);
        CodeAction removeObservesAsyncParamAction5 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", producesWithObservesAndObservesAsync,
                                                        removeObservesAsyncParam5);

        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, removeProducesAction5, removeObservesParamAction5, removeObservesAsyncParamAction5);

        JakartaJavaCodeActionParams codeActionParams6 = createCodeActionParams(uri, producesWithDisposesAndObservesAsync);

        TextEdit removeProduces6 = te(41, 4, 42, 4, "");
        TextEdit removeDisposesParam6 = te(42, 45, 42, 55, "");
        TextEdit removeObservesAsyncParam6 = te(42, 69, 42, 84, "");
        CodeAction removeProducesAction6 = ca(uri, "Remove @Produces", producesWithDisposesAndObservesAsync, removeProduces6);
        CodeAction removeDisposesParamAction6 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", producesWithDisposesAndObservesAsync, removeDisposesParam6);
        CodeAction removeObservesAsyncParamAction6 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", producesWithDisposesAndObservesAsync,
                                                        removeObservesAsyncParam6);

        assertJavaCodeAction(codeActionParams6, IJDT_UTILS, removeProducesAction6, removeDisposesParamAction6, removeObservesAsyncParamAction6);

        JakartaJavaCodeActionParams codeActionParams7 = createCodeActionParams(uri, producesWithAllThreeAnnotations);

        TextEdit removeProduces7 = te(47, 4, 48, 4, "");
        TextEdit removeDisposesParam7 = te(48, 53, 48, 63, "");
        TextEdit removeObservesParam7 = te(48, 77, 48, 87, "");
        TextEdit removeObservesAsyncParam7 = te(48, 101, 48, 116, "");
        CodeAction removeProducesAction7 = ca(uri, "Remove @Produces", producesWithAllThreeAnnotations, removeProduces7);
        CodeAction removeDisposesParamAction7 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", producesWithAllThreeAnnotations, removeDisposesParam7);
        CodeAction removeObservesParamAction7 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", producesWithAllThreeAnnotations, removeObservesParam7);
        CodeAction removeObservesAsyncParamAction7 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name3'", producesWithAllThreeAnnotations,
                                                        removeObservesAsyncParam7);

        assertJavaCodeAction(codeActionParams7, IJDT_UTILS, removeProducesAction7, removeDisposesParamAction7, removeObservesParamAction7, removeObservesAsyncParamAction7);

        JakartaJavaCodeActionParams codeActionParams8 = createCodeActionParams(uri, producesWithAllAnnotationsOnSameParam);

        TextEdit removeProduces8 = te(53, 4, 54, 4, "");
        TextEdit removeAllThreeParams8 = te(54, 54, 54, 89, "");
        CodeAction removeProducesAction8 = ca(uri, "Remove @Produces", producesWithAllAnnotationsOnSameParam, removeProduces8);
        CodeAction removeAllThreeParamsAction8 = ca(uri,
                                                    "Remove the '@Disposes', '@Observes', '@ObservesAsync' modifier from parameter 'name'", producesWithAllAnnotationsOnSameParam,
                                                    removeAllThreeParams8);

        assertJavaCodeAction(codeActionParams8, IJDT_UTILS, removeProducesAction8, removeAllThreeParamsAction8);

        JakartaJavaCodeActionParams codeActionParams9 = createCodeActionParams(uri, disposerWithObserves);

        TextEdit removeDisposesParam9 = te(30, 40, 30, 50, "");
        TextEdit removeObservesParam9 = te(30, 64, 30, 74, "");
        CodeAction removeDisposesParamAction9 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", disposerWithObserves, removeDisposesParam9);
        CodeAction removeObservesParamAction9 = ca(uri, "Remove the '@Observes' modifier from parameter 'name2'", disposerWithObserves, removeObservesParam9);

        assertJavaCodeAction(codeActionParams9, IJDT_UTILS, removeDisposesParamAction9, removeObservesParamAction9);

        JakartaJavaCodeActionParams codeActionParams10 = createCodeActionParams(uri, disposerWithObservesAsync);

        TextEdit removeDisposesParam10 = te(42, 45, 42, 55, "");
        TextEdit removeObservesAsyncParam10 = te(42, 69, 42, 84, "");
        CodeAction removeDisposesParamAction10 = ca(uri, "Remove the '@Disposes' modifier from parameter 'name1'", disposerWithObservesAsync, removeDisposesParam10);
        CodeAction removeObservesAsyncParamAction10 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'name2'", disposerWithObservesAsync, removeObservesAsyncParam10);

        assertJavaCodeAction(codeActionParams10, IJDT_UTILS, removeDisposesParamAction10, removeObservesAsyncParamAction10);
    }

    @Test
    public void multipleDisposes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/MultipleDisposes.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(9, 18, 23,
                         "The @Disposes annotation must not be defined on more than one parameter of a method.",
                         DiagnosticSeverity.Error, "jakarta-cdi", "InvalidDisposesAnnotationOnMultipleMethodParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }
}