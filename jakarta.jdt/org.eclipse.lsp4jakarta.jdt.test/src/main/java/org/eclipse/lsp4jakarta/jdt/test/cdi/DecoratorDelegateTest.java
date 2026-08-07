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
 * Tests for CDI decorator @Delegate injection point validation.
 *
 * A decorator must declare exactly one injection point annotated with @Delegate.
 *
 * @see https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#delegate_attribute
 */
public class DecoratorDelegateTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * Test that a decorator with multiple @Delegate fields triggers diagnostics at each field.
     *
     * Expected: Error on each @Delegate field indicating 2 @Delegate injection points found.
     */
    @Test
    public void testDecoratorWithMultipleDelegateFields() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/DecoratorWithMultipleDelegates.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Expected diagnostics on each @Delegate field
        // Line 16 (0-based: 15), field name "delegateA" starts at column 27, ends at column 36
        Diagnostic delegateADiagnostic = d(15, 27, 36,
                                           "A decorator must declare exactly one injection point annotated with @Delegate, but found 2.",
                                           DiagnosticSeverity.Error,
                                           "jakarta-cdi",
                                           "InvalidDecoratorDelegateInjectionPoints");

        // Line 20 (0-based: 19), field name "delegateB" starts at column 27, ends at column 36
        Diagnostic delegateBDiagnostic = d(19, 27, 36,
                                           "A decorator must declare exactly one injection point annotated with @Delegate, but found 2.",
                                           DiagnosticSeverity.Error,
                                           "jakarta-cdi",
                                           "InvalidDecoratorDelegateInjectionPoints");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, delegateADiagnostic, delegateBDiagnostic);
    }

    /**
     * Test that a decorator with no @Delegate injection point triggers a diagnostic.
     *
     * Expected: Error on class name indicating no @Delegate injection point.
     */
    @Test
    public void testDecoratorWithNoDelegate() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/DecoratorWithNoDelegate.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Expected diagnostic on class name
        // Line 11 (0-based: 10), class name "DecoratorWithNoDelegate" starts at column 13, ends at column 36
        Diagnostic noDelegateDiagnostic = d(10, 13, 36,
                                            "A decorator must declare exactly one injection point annotated with @Delegate.",
                                            DiagnosticSeverity.Error,
                                            "jakarta-cdi",
                                            "InvalidDecoratorDelegateInjectionPoints");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, noDelegateDiagnostic);
    }

    /**
     * Test that a decorator with @Delegate on both field and constructor parameter triggers diagnostics at each location.
     *
     * Expected: Error on field and parameter indicating 2 @Delegate injection points found.
     */
    @Test
    public void testDecoratorWithMixedDelegateInjectionPoints() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/DecoratorWithMixedDelegates.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Expected diagnostics on field and constructor parameter
        // Line 16 (0-based: 15), field name "delegateField" starts at column 27, ends at column 40
        Diagnostic fieldDelegateDiagnostic = d(15, 27, 40,
                                               "A decorator must declare exactly one injection point annotated with @Delegate, but found 2.",
                                               DiagnosticSeverity.Error,
                                               "jakarta-cdi",
                                               "InvalidDecoratorDelegateInjectionPoints");

        // Line 21 (0-based: 20), parameter name "delegate" starts at column 64, ends at column 72
        Diagnostic paramDelegateDiagnostic = d(20, 64, 72,
                                               "A decorator must declare exactly one injection point annotated with @Delegate, but found 2.",
                                               DiagnosticSeverity.Error,
                                               "jakarta-cdi",
                                               "InvalidDecoratorDelegateInjectionPoints");

        // DI diagnostic for constructor parameter
        Diagnostic diDiagnostic = d(20, 64, 72,
                                    "The parameter should define a constructor with no parameters or a constructor annotated with @Inject.",
                                    DiagnosticSeverity.Warning,
                                    "jakarta-di",
                                    "InjectionPointInvalidConstructorBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diDiagnostic, fieldDelegateDiagnostic, paramDelegateDiagnostic);
    }

    /**
     * Test that a valid decorator with exactly one @Delegate field does NOT trigger a diagnostic.
     *
     * Expected: No diagnostics.
     */
    @Test
    public void testValidDecoratorWithDelegateField() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/ValidDecorator.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected for valid decorator
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Test that a valid decorator with @Delegate on constructor parameter does NOT trigger a diagnostic.
     *
     * Expected: No diagnostics.
     */
    @Test
    public void testValidDecoratorWithDelegateOnConstructorParameter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/DecoratorWithDelegateOnConstructor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // This is a valid decorator for our diagnostic, but DI diagnostic warns about constructor parameter
        Diagnostic diDiagnostic = d(19, 71, 79,
                                    "The parameter should define a constructor with no parameters or a constructor annotated with @Inject.",
                                    DiagnosticSeverity.Warning,
                                    "jakarta-di",
                                    "InjectionPointInvalidConstructorBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, diDiagnostic);
    }

    /**
     * Test that a valid decorator with @Delegate on initializer method parameter does NOT trigger a diagnostic.
     *
     * Expected: No diagnostics.
     */
    @Test
    public void testValidDecoratorWithDelegateOnMethodParameter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/DecoratorWithDelegateOnMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected for valid decorator with method injection
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Test that @Delegate without @Inject on fields, methods, and constructors triggers diagnostics.
     *
     * Expected: Error on each @Delegate that is not accompanied by @Inject.
     */
    @Test
    public void testInvalidDelegateLocations() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/InvalidDelegateLocations.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Expected diagnostics for all three invalid @Delegate locations
        // Line 16 (0-based: 15), field name "delegate" starts at column 27, ends at column 35
        Diagnostic delegateFieldWithoutInjectDiagnostic = d(15, 27, 35,
                                                            "@Delegate must be applied to an injected field, or to a parameter of an initializer or constructor method.",
                                                            DiagnosticSeverity.Error,
                                                            "jakarta-cdi",
                                                            "InvalidDelegateInjectionPoint");

        // Line 33 (0-based: 32), method name "setDelegate" starts at column 16, ends at column 27
        Diagnostic delegateMethodParamWithoutInjectDiagnostic = d(32, 16, 27,
                                                                  "@Delegate must be applied to an injected field, or to a parameter of an initializer or constructor method.",
                                                                  DiagnosticSeverity.Error,
                                                                  "jakarta-cdi",
                                                                  "InvalidDelegateInjectionPoint");

        // Line 52 (0-based: 51), constructor name "DelegateOnNonInjectedConstructorParam" starts at column 11, ends at column 48
        Diagnostic delegateConstructorParamWithoutInjectDiagnostic = d(51, 11, 48,
                                                                       "@Delegate must be applied to an injected field, or to a parameter of an initializer or constructor method.",
                                                                       DiagnosticSeverity.Error,
                                                                       "jakarta-cdi",
                                                                       "InvalidDelegateInjectionPoint");

        // Additional diagnostics from other participants (CDI managed bean and DI)
        // Line 52 (0-based: 51), constructor declaration - managed bean constructor issue
        Diagnostic invalidManagedBeanConstructorDiagnostic = d(51, 11, 48,
                                                               "The @Inject annotation must define a managed bean constructor that takes parameters, or the managed bean must resolve to having a no-arg constructor instead.",
                                                               DiagnosticSeverity.Error,
                                                               "jakarta-cdi",
                                                               "InvalidManagedBeanWithInvalidConstructor");

        // Line 89 (0-based: 88), parameter in valid constructor - DI warning
        Diagnostic injectionPointConstructorBeanDiagnostic = d(88, 68, 76,
                                                               "The parameter should define a constructor with no parameters or a constructor annotated with @Inject.",
                                                               DiagnosticSeverity.Warning,
                                                               "jakarta-di",
                                                               "InjectionPointInvalidConstructorBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, injectionPointConstructorBeanDiagnostic,
                              invalidManagedBeanConstructorDiagnostic, delegateConstructorParamWithoutInjectDiagnostic,
                              delegateMethodParamWithoutInjectDiagnostic, delegateFieldWithoutInjectDiagnostic);

        // Test code actions for field without @Inject
        JakartaJavaCodeActionParams fieldCodeActionParams = createCodeActionParams(uri, delegateFieldWithoutInjectDiagnostic);
        TextEdit fieldInjectEdit = te(14, 4, 14, 4, "@Inject\n\t");
        CodeAction fieldInjectAction = ca(uri, "Insert @Inject", delegateFieldWithoutInjectDiagnostic, fieldInjectEdit);
        assertJavaCodeAction(fieldCodeActionParams, IJDT_UTILS, fieldInjectAction);

        // Test code actions for method parameter without @Inject
        JakartaJavaCodeActionParams methodCodeActionParams = createCodeActionParams(uri, delegateMethodParamWithoutInjectDiagnostic);
        TextEdit methodInjectEdit = te(32, 4, 32, 4, "@Inject\n\t");
        CodeAction methodInjectAction = ca(uri, "Insert @Inject", delegateMethodParamWithoutInjectDiagnostic, methodInjectEdit);
        assertJavaCodeAction(methodCodeActionParams, IJDT_UTILS, methodInjectAction);

        // Test code actions for constructor parameter without @Inject
        JakartaJavaCodeActionParams constructorCodeActionParams = createCodeActionParams(uri, delegateConstructorParamWithoutInjectDiagnostic);
        TextEdit constructorInjectEdit = te(51, 4, 51, 4, "@Inject\n\t");
        CodeAction constructorInjectAction = ca(uri, "Insert @Inject", delegateConstructorParamWithoutInjectDiagnostic, constructorInjectEdit);
        assertJavaCodeAction(constructorCodeActionParams, IJDT_UTILS, constructorInjectAction);
    }

    /**
     * Test that a decorator with delegate type that doesn't implement the decorated type triggers a diagnostic.
     *
     * Expected: Error on delegate field and method parameter indicating type mismatch.
     */
    @Test
    public void testDecoratorWithInvalidDelegateType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/DecoratorDelegateTypeAssignability.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Expected diagnostic on InvalidDelegateType class (field-level)
        // Line 18 (0-based: 17), field name "delegate" starts at column 19, ends at column 27
        Diagnostic invalidDelegateTypeDiagnostic = d(17, 19, 27,
                                                     "Delegate type 'Logger' does not implement the decorated type 'PaymentService'. A delegate must implement or extend the decorated type(s).",
                                                     DiagnosticSeverity.Error,
                                                     "jakarta-cdi",
                                                     "InvalidDecoratorDelegateTypeAssignability");

        // Expected diagnostic on InvalidDelegateTypePrimitive class (field-level)
        // Line 72 (0-based: 71), field name "delegate" starts at column 19, ends at column 27
        Diagnostic invalidDelegateTypePrimitiveDiagnostic = d(71, 19, 27,
                                                              "Delegate type 'String' does not implement the decorated type 'PaymentService'. A delegate must implement or extend the decorated type(s).",
                                                              DiagnosticSeverity.Error,
                                                              "jakarta-cdi",
                                                              "InvalidDecoratorDelegateTypeAssignability");

        // Expected diagnostic on InvalidDelegateTypeOnMethod class (method-level)
        // Line 91 (0-based: 90), parameter name "delegate" starts at column 45, ends at column 53
        Diagnostic invalidDelegateTypeOnMethodDiagnostic = d(90, 45, 53,
                                                             "Delegate type 'Logger' does not implement the decorated type 'PaymentService'. A delegate must implement or extend the decorated type(s).",
                                                             DiagnosticSeverity.Error,
                                                             "jakarta-cdi",
                                                             "InvalidDecoratorDelegateTypeAssignability");

        // Expected diagnostic on InvalidDelegateTypeTruePrimitive class (field-level)
        // Line 132 (0-based: 131), field name "delegate" starts at column 16, ends at column 24
        Diagnostic invalidDelegateTypeTruePrimitiveDiagnostic = d(131, 16, 24,
                                                                  "Delegate type 'int' does not implement the decorated type ''. A delegate must implement or extend the decorated type(s).",
                                                                  DiagnosticSeverity.Error,
                                                                  "jakarta-cdi",
                                                                  "InvalidDecoratorDelegateTypeAssignability");

        // Expected diagnostic on DecoratorWithDelegateButNoDecoratedTypes class (field-level): no decorated types exist
        // Line 152 (0-based: 151), field name "delegate" starts at column 27, ends at column 35
        Diagnostic noDecoratedTypesDiagnostic = d(151, 27, 35,
                                                  "Delegate type 'PaymentService' does not implement the decorated type ''. A delegate must implement or extend the decorated type(s).",
                                                  DiagnosticSeverity.Error,
                                                  "jakarta-cdi",
                                                  "InvalidDecoratorDelegateTypeAssignability");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, invalidDelegateTypeDiagnostic,
                              invalidDelegateTypePrimitiveDiagnostic, invalidDelegateTypeOnMethodDiagnostic,
                              invalidDelegateTypeTruePrimitiveDiagnostic, noDecoratedTypesDiagnostic);
    }

    /**
     * Test that a decorator with valid delegate type does NOT trigger a diagnostic.
     *
     * Expected: No diagnostics for valid delegate types.
     */
    @Test
    public void testDecoratorWithValidDelegateType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/decorator/ValidDecorator.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected for valid decorator with matching delegate type
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
