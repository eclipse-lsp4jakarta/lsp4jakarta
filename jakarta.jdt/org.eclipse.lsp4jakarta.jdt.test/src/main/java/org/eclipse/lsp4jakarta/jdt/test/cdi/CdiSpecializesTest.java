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

    // ── Diagnostic message builder ────────────────────────────────────────────
    // The engine resolves MessageFormat ''{0}'' → 'ClassName' (single quotes).
    // We therefore build the expected string with literal single quotes.
    private static String msg(String className) {
        return "Specialized bean '" + className + "' must not declare an explicit bean name using @Named. The name is inherited from the bean it specializes.";
    }

    @Test
    public void specializedBeanWithNamedAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SpecializedBeanWithNamed.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 14 (1-indexed) = line 13 (0-indexed), col 0..23
        // @Named("customService") = 23 chars
        Diagnostic namedDiagnostic = d(13, 0, 23,
                                       msg("SpecializedBeanWithNamed"),
                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSpecializedBeanWithNamedAnnotation");

        Diagnostic specializesDiagnostic = d(16, 13, 37,
                                             "A bean annotated with @Specializes must directly extend the bean class of another CDI managed bean with a scope annotation.",
                                             DiagnosticSeverity.Error,
                                             "jakarta-cdi",
                                             "InvalidSpecializesAnnotationOnNonBeanSuperclass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, namedDiagnostic, specializesDiagnostic);

        // Quick-fix: remove "@Named("customService")\n" (lines 13→14, col 0→0)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, namedDiagnostic);
        TextEdit removeNamedEdit = te(13, 0, 14, 0, "");
        CodeAction removeNamedAction = ca(uri, "Remove @Named", namedDiagnostic, removeNamedEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeNamedAction);
    }

    @Test
    public void specializedBeanWithBareNamedAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SpecializedBeanWithBareName.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 15 (0-indexed): @Specializes is first, then @Named on line 16 (0-indexed)
        // @Named alone = 6 chars; col 0..6
        Diagnostic namedDiagnostic = d(15, 0, 6,
                                       msg("SpecializedBeanWithBareName"),
                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSpecializedBeanWithNamedAnnotation");

        Diagnostic specializesDiagnostic = d(17, 13, 40,
                                             "A bean annotated with @Specializes must directly extend the bean class of another CDI managed bean with a scope annotation.",
                                             DiagnosticSeverity.Error,
                                             "jakarta-cdi",
                                             "InvalidSpecializesAnnotationOnNonBeanSuperclass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, namedDiagnostic, specializesDiagnostic);

        // Quick-fix: remove "@Named\n" (lines 15→16, col 0→0)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, namedDiagnostic);
        TextEdit removeNamedEdit = te(15, 0, 16, 0, "");
        CodeAction removeNamedAction = ca(uri, "Remove @Named", namedDiagnostic, removeNamedEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeNamedAction);
    }

    @Test
    public void validSpecializedBeanWithoutNamedAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ValidSpecializedBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — @Specializes without @Named is valid
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void namedBeanWithoutSpecializesAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/NamedWithoutSpecializes.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — @Named without @Specializes is valid
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that a class annotated with @Specializes with no superclass at all
     * triggers a diagnostic error.
     *
     * @Specializes requires directly extending a bean class. A class with no
     *              superclass cannot satisfy this requirement.
     */
    @Test
    public void testSpecializesWithNoSuperclass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SpecializesWithNoSuperclass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic noSuperclassDiagnostic = d(23, 13, 40,
                                              "A bean annotated with @Specializes must directly extend the bean class of another CDI managed bean with a scope annotation.",
                                              DiagnosticSeverity.Error,
                                              "jakarta-cdi",
                                              "InvalidSpecializesAnnotationOnNonBeanSuperclass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, noSuperclassDiagnostic);
    }

    /**
     * Tests that a class annotated with @Specializes that only implements an
     * interface (no extends clause) triggers a diagnostic error.
     *
     * Implementing an interface does not satisfy the CDI spec requirement that
     * the bean class must directly extend another bean class.
     */
    @Test
    public void testSpecializesWithInterfaceOnly() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SpecializesWithInterfaceOnly.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic interfaceOnlyDiagnostic = d(23, 13, 41,
                                               "A bean annotated with @Specializes must directly extend the bean class of another CDI managed bean with a scope annotation.",
                                               DiagnosticSeverity.Error,
                                               "jakarta-cdi",
                                               "InvalidSpecializesAnnotationOnNonBeanSuperclass");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, interfaceOnlyDiagnostic);
    }

    /**
     * Tests that a class annotated with @Specializes that extends a valid CDI bean
     * AND implements an interface does NOT trigger a diagnostic.
     *
     * The presence of the interface is irrelevant — what matters is that the direct
     * superclass is a valid CDI bean.
     */
    @Test
    public void testSpecializesWithBeanSuperclassAndInterface() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SpecializesWithBeanSuperclassAndInterface.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — direct superclass is a valid CDI bean
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
