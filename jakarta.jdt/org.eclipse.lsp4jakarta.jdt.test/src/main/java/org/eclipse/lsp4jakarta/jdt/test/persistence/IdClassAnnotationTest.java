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
 * Tests for the @IdClass / @Embeddable diagnostic.
 *
 * Specification: Jakarta Persistence 3.0, Section 11.1.14
 * "The primary key class must be annotated with the Embeddable annotation."
 *
 * @see https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a14687
 */
public class IdClassAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * A class annotated with @IdClass whose declared primary key class type does NOT carry @Embeddable
     * must produce a diagnostic at the @IdClass annotation range.
     */
    @Test
    public void testIdClassTypeNotAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/idclass/OrderWithInvalidIdClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 8 (0-based: 7): "@IdClass(OrderIdMissingEmbeddable.class) // ❌ OrderIdMissingEmbeddable lacks @Embeddable"
        // annotation starts at col 0, ends at col 40
        Diagnostic idClassMissingEmbeddableDiagnostic = d(7, 0, 40,
                                                          "The primary key class 'OrderIdMissingEmbeddable' used in the @IdClass annotation must be annotated with @Embeddable.",
                                                          DiagnosticSeverity.Error, "jakarta-persistence", "IdClassTypeNotAnnotatedWithEmbeddable");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, idClassMissingEmbeddableDiagnostic);
    }

    /**
     * A class annotated with @IdClass whose declared primary key class type IS annotated with @Embeddable
     * must NOT produce a diagnostic.
     */
    @Test
    public void testIdClassTypeAnnotatedWithEmbeddable() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/idclass/OrderWithValidIdClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostic expected: OrderIdWithEmbeddable is annotated with @Embeddable
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * A class annotated with @IdClass with a fully qualified primary key class type that does NOT carry @Embeddable
     * must produce a diagnostic at the @IdClass annotation range.
     */
    @Test
    public void testIdClassTypeNotAnnotatedWithEmbeddableFQ() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/idclass/OrderWithFQInvalidIdClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 8 (0-based: 7): "@IdClass(io.openliberty.sample.jakarta.persistence.idclass.OrderIdMissingEmbeddable.class) // ❌ OrderIdMissingEmbeddable lacks @Embeddable (FQ name)"
        // annotation starts at col 0, ends at col 90
        Diagnostic idClassMissingEmbeddableFQDiagnostic = d(7, 0, 90,
                                                            "The primary key class 'OrderIdMissingEmbeddable' used in the @IdClass annotation must be annotated with @Embeddable.",
                                                            DiagnosticSeverity.Error, "jakarta-persistence", "IdClassTypeNotAnnotatedWithEmbeddable");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, idClassMissingEmbeddableFQDiagnostic);
    }

    /**
     * A class annotated with @IdClass with a fully qualified primary key class type that IS annotated with @Embeddable
     * must NOT produce a diagnostic.
     */
    @Test
    public void testIdClassTypeAnnotatedWithEmbeddableFQ() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/idclass/OrderWithFQValidIdClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostic expected: OrderIdWithEmbeddable is annotated with @Embeddable (FQ name)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
