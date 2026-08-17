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
}
