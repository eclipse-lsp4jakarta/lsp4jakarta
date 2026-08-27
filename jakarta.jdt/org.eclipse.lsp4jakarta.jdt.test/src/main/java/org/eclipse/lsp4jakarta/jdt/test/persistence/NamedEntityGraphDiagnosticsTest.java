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
package org.eclipse.lsp4jakarta.jdt.test.persistence;

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
 * Integration tests for {@code @NamedEntityGraph} uniqueness rule.
 *
 * <p>Rule: Graph names must be unique within the persistence unit.
 * Spec §3.7.4:
 * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13662">a13662</a>
 */
public class NamedEntityGraphDiagnosticsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * Tests that an entity class with a {@code @NamedEntityGraph} whose name
     * duplicates a graph name declared on another entity in the same project
     * produces a {@code DuplicateNamedEntityGraphName} diagnostic.
     *
     * <p>Both {@code NamedEntityGraphDuplicate1} and {@code NamedEntityGraphDuplicate2}
     * declare a graph named {@code "User.graph"}. Each file must independently
     * produce a diagnostic for its own annotation.
     *
     * Spec §3.7.4:
     * https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13662
     */
    @Test
    public void duplicateNamedEntityGraphNameInDuplicate1() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphDuplicate1.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @NamedEntityGraph(name = "User.graph") is at 0-based line 7, cols 0-38.
        Diagnostic duplicate = d(7, 0, 38,
                                 "The @NamedEntityGraph name 'User.graph' must be unique within the persistence unit.",
                                 DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateNamedEntityGraphName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, duplicate);
    }

    /**
     * Tests that the second entity class with the same duplicate {@code @NamedEntityGraph}
     * name also produces a diagnostic.
     */
    @Test
    public void duplicateNamedEntityGraphNameInDuplicate2() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphDuplicate2.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @NamedEntityGraph(name = "User.graph") is at 0-based line 7, cols 0-38.
        Diagnostic duplicate = d(7, 0, 38,
                                 "The @NamedEntityGraph name 'User.graph' must be unique within the persistence unit.",
                                 DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateNamedEntityGraphName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, duplicate);
    }

    /**
     * Tests that an entity with a unique {@code @NamedEntityGraph} name does NOT
     * produce any diagnostic.
     *
     * Spec §3.7.4:
     * https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13662
     */
    @Test
    public void uniqueNamedEntityGraphNameValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphUnique.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — "Admin.uniqueGraph" is unique across the project.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    // -------------------------------------------------------------------------
    // Guard: files that must bypass the project-wide scan
    // -------------------------------------------------------------------------

    /**
     * A plain class with no persistence annotations must not produce any
     * diagnostic — and the file-level guard must prevent the project-wide
     * scan from being triggered at all.
     */
    @Test
    public void nonEntityClassNoScanTriggered() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NonEntityClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No @Entity + no @NamedEntityGraph(s) → guard fires, no diagnostics.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * An {@code @Entity} class that has neither {@code @NamedEntityGraph} nor
     * {@code @NamedEntityGraphs} must not produce any diagnostic — and the
     * file-level guard must prevent the project-wide scan from being triggered.
     */
    @Test
    public void entityWithoutGraphAnnotationNoScanTriggered() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EntityWithoutGraphAnnotation.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @Entity present but no @NamedEntityGraph(s) → guard fires, no diagnostics.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    // -------------------------------------------------------------------------
    // @NamedEntityGraphs container annotation
    // -------------------------------------------------------------------------

    /**
     * Entity using {@code @NamedEntityGraphs} where one inner graph duplicates
     * {@code "User.graph"} (also declared in {@code NamedEntityGraphDuplicate1}
     * and {@code NamedEntityGraphDuplicate2}).
     *
     * <p>The inner {@code @NamedEntityGraph(name = "User.graph")} is at
     * 0-based line 9, cols 8–46 in the test resource file.
     * The inner {@code @NamedEntityGraph(name = "Container.uniqueGraph")} is unique
     * and must not produce a diagnostic.
     */
    @Test
    public void namedEntityGraphsContainerWithDuplicateName() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphsContainerDuplicate.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // inner @NamedEntityGraph(name = "User.graph") at 0-based line 9, cols 8-46
        Diagnostic duplicateContainerGraphDiag = d(9, 8, 46,
                                                   "The @NamedEntityGraph name 'User.graph' must be unique within the persistence unit.",
                                                   DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateNamedEntityGraphName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, duplicateContainerGraphDiag);
    }

    /**
     * Entity using {@code @NamedEntityGraphs} where both inner graphs have
     * unique names — no diagnostics expected.
     */
    @Test
    public void namedEntityGraphsContainerAllUnique() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphsContainerUnique.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Both "Container.graphA" and "Container.graphB" are unique — no diagnostics.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Entity using {@code @NamedEntityGraphs} where both inner graphs share the
     * same name {@code "Self.duplicate"} — both must produce a diagnostic.
     *
     * <p>Both inner annotations are at 0-based line 9 and 10 respectively,
     * each starting at col 8.
     * {@code @NamedEntityGraph(name = "Self.duplicate")} = 42 chars → cols 8–50.
     */
    @Test
    public void namedEntityGraphsSelfDuplicateBothFlagged() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/NamedEntityGraphsSelfDuplicate.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Both inner @NamedEntityGraph(name = "Self.duplicate") are at lines 9 and 10, cols 8-50
        Diagnostic firstSelfDuplicateDiag = d(9, 8, 50,
                                              "The @NamedEntityGraph name 'Self.duplicate' must be unique within the persistence unit.",
                                              DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateNamedEntityGraphName");
        Diagnostic secondSelfDuplicateDiag = d(10, 8, 50,
                                               "The @NamedEntityGraph name 'Self.duplicate' must be unique within the persistence unit.",
                                               DiagnosticSeverity.Error, "jakarta-persistence", "DuplicateNamedEntityGraphName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, firstSelfDuplicateDiag, secondSelfDuplicateDiag);
    }
}
