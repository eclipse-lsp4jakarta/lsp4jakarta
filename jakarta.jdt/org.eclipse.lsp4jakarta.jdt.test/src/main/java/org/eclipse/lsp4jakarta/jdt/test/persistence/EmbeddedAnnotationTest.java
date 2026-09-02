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
 * Tests for the @Embedded / @Embeddable diagnostic.
 *
 * Specification: Jakarta Persistence 3.0, Section 11.1.16
 * "The embeddable class must be annotated as Embeddable."
 *
 * @see https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a14672
 */
public class EmbeddedAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * A field annotated with @Embedded whose declared type does NOT carry @Embeddable
     * must produce a diagnostic at the field name.
     */
    @Test
    public void testEmbeddedFieldTypeNotAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EmbeddedWithoutEmbeddable.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 14 (0-based: 13): "    private EmbeddedAddress address;"
        // "address" starts at col 28, ends at col 35
        Diagnostic embeddedFieldNotEmbeddableD = d(13, 28, 35,
                                                   "The type 'EmbeddedAddress' used in the @Embedded field or property must be annotated with @Embeddable.",
                                                   DiagnosticSeverity.Error, "jakarta-persistence", "EmbeddedTypeNotAnnotatedWithEmbeddable");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, embeddedFieldNotEmbeddableD);
    }

    /**
     * A field annotated with @Embedded whose declared type IS annotated with @Embeddable
     * must NOT produce a diagnostic.
     */
    @Test
    public void testEmbeddedFieldTypeAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EmbeddedWithEmbeddable.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostic expected: AddressWithEmbedded is annotated with @Embeddable
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * A method annotated with @Embedded whose declared type does NOT carry @Embeddable
     * must produce a diagnostic at the method name.
     */
    @Test
    public void testEmbeddedMethodTypeNotAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EmbeddedWithoutEmbeddableOnMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 19 (0-based: 18): "    public EmbeddedAddress getAddress() {"
        // "getAddress" starts at col 27, ends at col 37
        Diagnostic embeddedMethodNotEmbeddableD = d(18, 27, 37,
                                                    "The type 'EmbeddedAddress' used in the @Embedded field or property must be annotated with @Embeddable.",
                                                    DiagnosticSeverity.Error, "jakarta-persistence", "EmbeddedTypeNotAnnotatedWithEmbeddable");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, embeddedMethodNotEmbeddableD);
    }

    /**
     * A method annotated with @Embedded whose declared type IS annotated with @Embeddable
     * must NOT produce a diagnostic.
     */
    @Test
    public void testEmbeddedMethodTypeAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/EmbeddedWithEmbeddableOnMethod.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostic expected: AddressWithEmbedded is annotated with @Embeddable
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
