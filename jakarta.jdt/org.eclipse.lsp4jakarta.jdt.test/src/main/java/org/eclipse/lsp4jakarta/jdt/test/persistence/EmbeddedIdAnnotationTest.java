/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available available under the
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
 * Tests for the @EmbeddedId / @Embeddable diagnostic.
 *
 * Specification: Jakarta Persistence 3.0, Section 11.1.14
 * "The primary key class must be annotated with the Embeddable annotation."
 *
 * @see https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a14687
 */
public class EmbeddedIdAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * A field annotated with @EmbeddedId whose declared type does NOT carry @Embeddable
     * must produce a diagnostic at the field name.
     */
    @Test
    public void testEmbeddedIdFieldTypeNotAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/embeddedid/EmployeeWithInvalidEmbeddedId.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 10 (0-based: 9): "    private EmployeeIdMissingEmbeddable id;"
        // "id" starts at col 40, ends at col 42
        Diagnostic expectedDiagnostic = d(9, 40, 42,
                                          "The composite primary key class 'EmployeeIdMissingEmbeddable' used in the @EmbeddedId field or property must be annotated with @Embeddable.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "EmbeddedIdTypeNotAnnotatedWithEmbeddable");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);
    }

    /**
     * A field annotated with @EmbeddedId whose declared type IS annotated with @Embeddable
     * must NOT produce a diagnostic.
     */
    @Test
    public void testEmbeddedIdFieldTypeAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/embeddedid/EmployeeWithValidEmbeddedId.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostic expected: EmployeeIdWithEmbeddable is annotated with @Embeddable
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * A method annotated with @EmbeddedId whose declared type does NOT carry @Embeddable
     * must produce a diagnostic at the method name.
     */
    @Test
    public void testEmbeddedIdMethodTypeNotAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/embeddedid/EmployeeWithInvalidEmbeddedIdOnMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 12 (0-based: 11): "    public EmployeeIdMissingEmbeddable getId() {"
        // "getId" starts at col 39, ends at col 44
        Diagnostic expectedDiagnostic = d(11, 39, 44,
                                          "The composite primary key class 'EmployeeIdMissingEmbeddable' used in the @EmbeddedId field or property must be annotated with @Embeddable.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "EmbeddedIdTypeNotAnnotatedWithEmbeddable");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);
    }

    /**
     * A method annotated with @EmbeddedId whose declared type IS annotated with @Embeddable
     * must NOT produce a diagnostic.
     */
    @Test
    public void testEmbeddedIdMethodTypeAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/embeddedid/EmployeeWithValidEmbeddedIdOnMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostic expected: EmployeeIdWithEmbeddable is annotated with @Embeddable
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
