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
 * Tests for CDI {@code @Typed} annotation validation.
 *
 * <p>Per CDI 3.0 specification §2.2.2, if a bean class, producer method, or producer field
 * specifies a {@code @Typed} annotation, and the {@code value} member specifies a class that
 * does not correspond to a type in the unrestricted set of bean types of a bean, the container
 * automatically detects the problem and treats it as a definition error.</p>
 *
 * <p>Fixture files used by these tests:</p>
 * <ul>
 * <li>{@code TypedBeanClassInvalid.java} — bean class: one invalid type → 1 diagnostic</li>
 * <li>{@code TypedProducerMethodInvalid.java} — producer method: invalid return type → 1 diagnostic</li>
 * <li>{@code TypedProducerFieldInvalid.java} — producer field: invalid field type → 1 diagnostic</li>
 * <li>{@code TypedBeanClassMultipleValues.java}— bean class: mix of valid + invalid → 1 diagnostic for invalid only</li>
 * <li>{@code TypedBeanClassValid.java} — bean class: valid interface → no diagnostics</li>
 * <li>{@code TypedBeanClassSelf.java} — bean class: the class itself → no diagnostics</li>
 * <li>{@code TypedBeanClassSupertype.java} — bean class: a superclass → no diagnostics</li>
 * <li>{@code TypedBeanClassEmpty.java} — bean class: empty array → no diagnostics</li>
 * <li>{@code TypedProducerMethodValid.java} — producer method: valid return type → no diagnostics</li>
 * <li>{@code TypedProducerFieldValid.java} — producer field: valid field type → no diagnostics</li>
 * <li>{@code TypedProducerMethodGenericReturnType.java} — producer method: type-variable return type → no diagnostics</li>
 * </ul>
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
public class CdiTypedAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    private static String msg(String typeName) {
        return "The @Typed annotation specifies '" + typeName + "' which is not part of the unrestricted set of bean types for this bean.";
    }

    // ── Invalid cases ─────────────────────────────────────────────────────────

    @Test
    public void typedAnnotationOnBeanClassInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedBeanClassInvalid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 15 (0-indexed = 14): @Typed(String.class) at col 0..20
        Diagnostic typedDiagnostic = d(14, 0, 20,
                                       msg("String"),
                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidTypedAnnotationNonMatchingBeanType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, typedDiagnostic);
    }

    @Test
    public void typedAnnotationOnProducerMethodInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedProducerMethodInvalid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 21 (0-indexed = 20): "    @Typed(String.class)" at col 4..24
        Diagnostic typedDiagnostic = d(20, 4, 24,
                                       msg("String"),
                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidTypedAnnotationNonMatchingBeanType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, typedDiagnostic);
    }

    @Test
    public void typedAnnotationOnProducerFieldInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedProducerFieldInvalid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 18 (0-indexed = 17): "    @Typed(String.class)" at col 4..24
        Diagnostic typedDiagnostic = d(17, 4, 24,
                                       msg("String"),
                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidTypedAnnotationNonMatchingBeanType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, typedDiagnostic);
    }

    @Test
    public void typedAnnotationMultipleValuesOnlyInvalidFlagged() throws Exception {
        // @Typed({ TypedShop.class, String.class }) — TypedShop is valid (implemented),
        // String is invalid → only String should produce a diagnostic.
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedBeanClassMultipleValues.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 14 (0-indexed = 13): @Typed({ TypedShop.class, String.class }) at col 0..41
        Diagnostic typedDiagnostic = d(13, 0, 41,
                                       msg("String"),
                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidTypedAnnotationNonMatchingBeanType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, typedDiagnostic);
    }

    // ── Valid cases ───────────────────────────────────────────────────────────

    @Test
    public void typedAnnotationOnBeanClassValid() throws Exception {
        // @Typed(TypedShop.class) — TypedShop is a directly implemented interface → valid
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedBeanClassValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void typedAnnotationBeanClassSelfIsValid() throws Exception {
        // @Typed(TypedBeanClassSelf.class) — the class itself is always in its own unrestricted types → valid
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedBeanClassSelf.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void typedAnnotationBeanClassSupertypeIsValid() throws Exception {
        // @Typed(TypedBeanBase.class) — TypedBeanBase is a direct superclass → valid
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedBeanClassSupertype.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void typedAnnotationEmptyArrayIsValid() throws Exception {
        // @Typed({}) — empty array means no types to validate → valid (no diagnostics)
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedBeanClassEmpty.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void typedAnnotationOnProducerMethodValid() throws Exception {
        // @Typed(List.class) on a method returning List<String> → valid
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedProducerMethodValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void typedAnnotationOnProducerFieldValid() throws Exception {
        // @Typed(List.class) on a field of type List<String> → valid
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedProducerFieldValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void typedAnnotationOnProducerMethodWithGenericReturnTypeIsValid() throws Exception {
        // @Typed(Object.class) on a method returning a type variable T — the return type
        // cannot be resolved to a concrete IType, so no diagnostic should be raised.
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedProducerMethodGenericReturnType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    // ── Quickfix tests ────────────────────────────────────────────────────────

    @Test
    public void removeTypedAnnotationQuickFixOnBeanClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedBeanClassInvalid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        Diagnostic typedDiagnostic = d(14, 0, 20,
                                       msg("String"),
                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidTypedAnnotationNonMatchingBeanType");

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, typedDiagnostic);
        // Remove the entire "@Typed(String.class)\n" line
        TextEdit removeTyped = te(14, 0, 15, 0, "");
        assertJavaCodeAction(codeActionParams, IJDT_UTILS,
                             ca(uri, "Remove @Typed", typedDiagnostic, removeTyped));
    }

    @Test
    public void removeTypedAnnotationQuickFixOnProducerField() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/TypedProducerFieldInvalid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        Diagnostic typedDiagnostic = d(17, 4, 24,
                                       msg("String"),
                                       DiagnosticSeverity.Error, "jakarta-cdi", "InvalidTypedAnnotationNonMatchingBeanType");

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, typedDiagnostic);
        TextEdit removeTyped = te(17, 4, 18, 4, "");
        assertJavaCodeAction(codeActionParams, IJDT_UTILS,
                             ca(uri, "Remove @Typed", typedDiagnostic, removeTyped));
    }
}
