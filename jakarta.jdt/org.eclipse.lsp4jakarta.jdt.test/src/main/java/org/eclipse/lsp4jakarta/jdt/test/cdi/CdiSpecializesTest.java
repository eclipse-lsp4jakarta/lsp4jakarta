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
 * Tests for CDI @Specializes validation.
 *
 * A bean annotated with @Specializes must extend another bean (one with a CDI scope annotation).
 * If the superclass is not a bean, a definition error diagnostic must be reported.
 *
 * @see https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization
 */
public class CdiSpecializesTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * Tests that a class annotated with @Specializes that extends a class with no scope annotation
     * triggers a diagnostic error.
     *
     * Expected: Error on class name indicating superclass is not a bean.
     */
    @Test
    public void testSpecializesWithNonBeanSuperclass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SpecializesWithNonBeanSuperclass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 24 (1-based) = line 23 (0-based)
        // class name "SpecializesWithNonBeanSuperclass" starts at col 13, ends at col 45
        Diagnostic unscopedSuperclassDiagnostic = d(23, 13, 45,
                                                    "A bean annotated with @Specializes must directly extend the bean class of another CDI managed bean with a scope annotation.",
                                                    DiagnosticSeverity.Error,
                                                    "jakarta-cdi",
                                                    "InvalidSpecializesAnnotationOnNonBeanSuperclass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, unscopedSuperclassDiagnostic);
    }

    /**
     * Tests that a class annotated with @Specializes that extends a valid CDI bean
     * (with @ApplicationScoped) does NOT trigger a diagnostic.
     *
     * Expected: No diagnostics.
     */
    @Test
    public void testSpecializesWithValidBeanSuperclass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SpecializesWithBeanSuperclass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected for valid specialization
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that a class annotated with @Specializes whose direct superclass has no scope
     * annotation triggers a diagnostic, even though the grandparent class IS a valid CDI bean.
     *
     * CDI spec 3.1.4 requires only the *direct* (immediate) superclass to be a bean.
     * A scoped grandparent does NOT satisfy this requirement.
     *
     * Expected: Error on class name indicating the direct superclass is not a bean.
     */
    @Test
    public void testSpecializesWithGrandparentBeanOnly() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SpecializesWithGrandparentBeanOnly.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 26 (1-based) = line 25 (0-based)
        // "SpecializesWithGrandparentBeanOnly" starts at col 13, ends at col 47 (34 chars)
        Diagnostic scopedGrandparentOnlyDiagnostic = d(25, 13, 47,
                                                       "A bean annotated with @Specializes must directly extend the bean class of another CDI managed bean with a scope annotation.",
                                                       DiagnosticSeverity.Error,
                                                       "jakarta-cdi",
                                                       "InvalidSpecializesAnnotationOnNonBeanSuperclass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, scopedGrandparentOnlyDiagnostic);
    }
}
