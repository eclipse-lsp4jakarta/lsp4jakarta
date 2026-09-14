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
 * Tests for CDI @Named qualifier validation on injection points.
 *
 * According to CDI specification section 3.9:
 * - @Named on field injection without value is VALID (field name is assumed)
 * - @Named on constructor/method parameters without value is INVALID (definition error)
 */
public class NamedQualifierInjectionPointTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void namedQualifierOnInjectionPoints() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/NamedQualifierInjectionPoint.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostics for @Named without value on non-field injection points

        // Line 29 (0-based: 28): @Named on constructor parameter without value
        Diagnostic constructorParamDiagnostic = d(28, 40, 46,
                                                  "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        // Line 36 (0-based: 35): @Named on method parameter without value
        Diagnostic methodParamDiagnostic = d(35, 28, 34,
                                             "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                             DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        // Line 43 (0-based: 42): @Named on initializer method parameter without value
        Diagnostic initializerParamDiagnostic = d(42, 27, 33,
                                                  "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, constructorParamDiagnostic, methodParamDiagnostic, initializerParamDiagnostic);

        // Test code action for constructorParamDiagnostic - Insert value attribute
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, constructorParamDiagnostic);
        TextEdit insertValueConstructorEdit = te(28, 40, 28, 46, "@Named(value = \"\")");
        CodeAction insertValueConstructorAction = ca(uri, "Insert 'value' attribute to @Named", constructorParamDiagnostic, insertValueConstructorEdit);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, insertValueConstructorAction);

        // Test code action for methodParamDiagnostic - Insert value attribute
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, methodParamDiagnostic);
        TextEdit insertValueMethodEdit = te(35, 28, 35, 34, "@Named(value = \"\")");
        CodeAction insertValueMethodAction = ca(uri, "Insert 'value' attribute to @Named", methodParamDiagnostic, insertValueMethodEdit);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, insertValueMethodAction);

        // Test code action for initializerParamDiagnostic - Insert value attribute
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, initializerParamDiagnostic);
        TextEdit insertValueInitializerEdit = te(42, 27, 42, 33, "@Named(value = \"\")");
        CodeAction insertValueInitializerAction = ca(uri, "Insert 'value' attribute to @Named", initializerParamDiagnostic, insertValueInitializerEdit);
        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, insertValueInitializerAction);
    }

    @Test
    public void validNamedInjectionPoints() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ValidNamedInjectionPoint.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // This file contains only VALID uses of @Named - should produce NO diagnostics
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void namedInjectionPointComprehensive() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/NamedInjectionPoint.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostics for comprehensive test file with nested classes

        // Line 22 (0-based: 21): @Named on constructor parameter without value
        Diagnostic constructorDiagnostic = d(21, 31, 37,
                                             "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                             DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        // Line 36 (0-based: 35): @Named on method parameter without value
        Diagnostic methodDiagnostic = d(35, 28, 34,
                                        "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                        DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        // Line 49 (0-based: 48): First @Named in nested class with multiple params
        Diagnostic nestedClass1Diagnostic = d(48, 37, 43,
                                              "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                              DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        // Line 49 (0-based: 48): Second @Named in nested class with multiple params
        Diagnostic nestedClass2Diagnostic = d(48, 61, 67,
                                              "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                              DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        // Line 57 (0-based: 56): @Named without value in mixed params nested class
        Diagnostic mixedParamDiagnostic = d(56, 60, 66,
                                            "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        // Line 64 (0-based: 63): @Named on initializer method parameter without value
        Diagnostic initializerDiagnostic = d(63, 27, 33,
                                             "The @Named annotation on this injection point must specify a value. Only field injection points can omit the value (field name is assumed).",
                                             DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNamedAnnotationOnNonFieldInjectionPoint");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, constructorDiagnostic, methodDiagnostic,
                              nestedClass1Diagnostic, nestedClass2Diagnostic, mixedParamDiagnostic, initializerDiagnostic);

        // Test code action for one of the diagnostics
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, constructorDiagnostic);
        TextEdit insertValueEdit = te(21, 31, 21, 37, "@Named(value = \"\")");
        CodeAction insertValueAction = ca(uri, "Insert 'value' attribute to @Named", constructorDiagnostic, insertValueEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertValueAction);
    }
}
