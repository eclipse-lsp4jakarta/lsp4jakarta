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
 * Tests for CDI diagnostics and quick-fixes for orphan disposer methods —
 * disposer methods that have no matching @Produces producer in the same class.
 *
 * Specification reference:
 * https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#disposer_method_resolution
 */
public class OrphanDisposerTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * A class that has a @Disposes method but no @Produces producer must produce
     * an {@code InvalidOrphanDisposerMethod} diagnostic on the disposer method.
     */
    @Test
    public void orphanDisposerNoDiagnosticWhenProducerMethodPresent() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ValidProducerDisposer.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No orphan-disposer diagnostic expected — @Produces method and disposer are co-located.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * A class where the matching producer is a {@code @Produces} field (not a method)
     * must also produce no {@code InvalidOrphanDisposerMethod} diagnostic.
     */
    @Test
    public void orphanDisposerNoDiagnosticWhenProducerFieldPresent() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ValidProducerFieldDisposer.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No orphan-disposer diagnostic expected — @Produces field and disposer are co-located.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * A class with a @Disposes method but no matching @Produces must produce a
     * diagnostic on the disposer method, and the quick-fix must remove the @Disposes
     * annotation from the parameter.
     */
    @Test
    public void orphanDisposerDiagnosticAndQuickFix() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/OrphanDisposer.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // OrphanDisposer.java — cleanup() is at line 12 (1-based) = line index 11 (0-based).
        // Method name "cleanup" starts at column 16, ends at column 23.
        Diagnostic orphanDiag = d(11, 16, 23,
                                  "A disposer method must have a corresponding producer method or producer field in the same class. The @Disposes parameter type 'Connection' has no matching @Produces method or field in this class.",
                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidOrphanDisposerMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, orphanDiag);

        // Quick-fix: remove the @Disposes annotation from parameter 'conn'.
        // Line 11 (0-based): "    public void cleanup(@Disposes Connection conn) {"
        // "@Disposes " occupies columns 24-34 (0-based, includes trailing space)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, orphanDiag);
        TextEdit removeDisposes = te(11, 24, 11, 34, "");
        CodeAction removeDisposesAction = ca(uri, "Remove the '@Disposes' modifier from parameter 'conn'", orphanDiag, removeDisposes);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeDisposesAction);
    }

    /**
     * A class that produces type {@code Connection} but whose disposer targets a
     * different type {@code Session} must be flagged as an orphan — the producer and
     * disposer types do not match.
     */
    @Test
    public void orphanDisposerWhenProducerAndDisposerTypesDoNotMatch() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/CrossTypeOrphanDisposer.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // CrossTypeOrphanDisposer.java — cleanupSession() is at line 22 (1-based) = index 21 (0-based).
        // Method name "cleanupSession" starts at column 16, ends at column 30.
        Diagnostic orphanDiag = d(21, 16, 30,
                                  "A disposer method must have a corresponding producer method or producer field in the same class. The @Disposes parameter type 'Session' has no matching @Produces method or field in this class.",
                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidOrphanDisposerMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, orphanDiag);
    }

    /**
     * A class with multiple producers of different types where the disposer targets
     * exactly one of those types (via a {@code @Produces} field) must produce
     * no {@code InvalidOrphanDisposerMethod} diagnostic.
     */
    @Test
    public void orphanDisposerNoDiagnosticWhenOneOfMultipleProducersMatchesDisposer() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/MultiProducerDisposer.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No orphan-disposer diagnostic expected — the @Produces Session field matches the disposer.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
