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

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
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
}
