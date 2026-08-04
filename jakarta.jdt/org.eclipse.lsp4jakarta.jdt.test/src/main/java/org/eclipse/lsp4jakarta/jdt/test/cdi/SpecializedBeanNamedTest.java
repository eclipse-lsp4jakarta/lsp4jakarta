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
 * Tests for CDI specialized beans that declare an explicit @Named annotation.
 *
 * <p>Per CDI 3.0 specification §4.3 (direct and indirect specialization), a bean
 * annotated with {@code @Specializes} inherits the bean name of the bean it
 * overrides. Declaring {@code @Named} on a specialized bean is therefore invalid
 * and the container will treat it as a definition error.</p>
 *
 * <p>Fixture files used by these tests:</p>
 * <ul>
 * <li>{@code SpecializedBeanWithNamed.java} — outer class + inner class, both invalid</li>
 * <li>{@code SpecializedBeanWithBareName.java} — bare {@code @Named} (no value), invalid</li>
 * <li>{@code ValidSpecializedBean.java} — {@code @Specializes} without {@code @Named}, valid</li>
 * <li>{@code NamedWithoutSpecializes.java} — {@code @Named} without {@code @Specializes}, valid</li>
 * </ul>
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §4.3</a>
 */
public class SpecializedBeanNamedTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

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

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, namedDiagnostic);

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

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, namedDiagnostic);

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
}
