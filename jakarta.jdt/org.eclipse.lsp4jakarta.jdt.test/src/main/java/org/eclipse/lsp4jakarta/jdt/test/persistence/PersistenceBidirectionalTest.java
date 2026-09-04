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
 * Integration tests for {@code PersistenceBidirectionalDiagnosticsParticipant}
 * — bidirectional JPA relationship validation (issue #726).
 *
 * <p>Two rules from Jakarta Persistence 3.0 §2.9 are verified:
 * <ol>
 * <li>The inverse side of a bidirectional {@code @OneToMany}, {@code @OneToOne},
 * or {@code @ManyToMany} relationship must declare {@code mappedBy}.</li>
 * <li>The inverse side must not carry a {@code @JoinTable} annotation.</li>
 * </ol>
 */
public class PersistenceBidirectionalTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -------------------------------------------------------------------------
    // Rule 1 — inverse side missing mappedBy
    // -------------------------------------------------------------------------

    @Test
    public void testBidirectionalMissingMappedByOneToMany() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        // The owning-side file must be loaded so the cross-file scan finds the @ManyToOne back-reference.
        javaProject.getProject().getFile(
                                         new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalEmployee.java"));

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalDepartment.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @OneToMany without mappedBy — BidirectionalEmployee has a @ManyToOne back-ref.
        // Expected: diagnostic on the employees field name (line 23, col 40..49)
        Diagnostic missingMappedByDiagnostic = d(23, 40, 49,
                                                 "The inverse side of a bidirectional @OneToMany relationship must declare the mappedBy attribute to reference the owning side field.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "InverseSideMissingMappedBy");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, missingMappedByDiagnostic);
    }

    @Test
    public void testBidirectionalMissingMappedByOneToOne() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        // The owning-side file must be loaded so the cross-file scan finds the @OneToOne back-reference.
        javaProject.getProject().getFile(
                                         new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalAddress.java"));

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalPerson.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @OneToOne without mappedBy — BidirectionalAddress has a @OneToOne back-ref.
        // Expected: diagnostic on the address field name (line 21, col 33..40)
        Diagnostic missingMappedByDiagnostic = d(21, 33, 40,
                                                 "The inverse side of a bidirectional @OneToOne relationship must declare the mappedBy attribute to reference the owning side field.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "InverseSideMissingMappedBy");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, missingMappedByDiagnostic);
    }

    @Test
    public void testBidirectionalMissingMappedByManyToMany() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        // The owning-side file must be loaded so the cross-file scan finds the @ManyToMany back-ref.
        javaProject.getProject().getFile(
                                         new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalStudent.java"));

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalCourse.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @ManyToMany without mappedBy — BidirectionalStudent has a @ManyToMany back-ref.
        // Expected: diagnostic on the students field name (line 22, col 39..47)
        Diagnostic manyToManyMissingMappedByDiagnostic = d(22, 39, 47,
                                                           "The inverse side of a bidirectional @ManyToMany relationship must declare the mappedBy attribute to reference the owning side field.",
                                                           DiagnosticSeverity.Error, "jakarta-persistence", "InverseSideMissingMappedBy");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, manyToManyMissingMappedByDiagnostic);
    }

    // -------------------------------------------------------------------------
    // Rule 2 — @JoinTable on the inverse side
    // -------------------------------------------------------------------------

    @Test
    public void testBidirectionalJoinTableOnOneToManyInverse() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalInverseJoinTable.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @OneToMany(mappedBy=...) + @JoinTable on the inverse side — invalid.
        // Expected: diagnostic on the employees field name (line 25, col 40..49)
        Diagnostic joinTableOnInverseDiagnostic = d(25, 40, 49,
                                                    "The @JoinTable annotation must not be used on the inverse side of a relationship. Move @JoinTable to the owning side field or remove it.",
                                                    DiagnosticSeverity.Error, "jakarta-persistence", "JoinTableOnInverseSide");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, joinTableOnInverseDiagnostic);
    }

    @Test
    public void testBidirectionalJoinTableOnManyToManyInverse() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalPost.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @ManyToMany(mappedBy=...) + @JoinTable on the inverse side — invalid.
        // Expected: diagnostic on the tags field name (line 24, col 35..39)
        Diagnostic joinTableOnManyToManyInverseDiagnostic = d(24, 35, 39,
                                                              "The @JoinTable annotation must not be used on the inverse side of a relationship. Move @JoinTable to the owning side field or remove it.",
                                                              DiagnosticSeverity.Error, "jakarta-persistence", "JoinTableOnInverseSide");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, joinTableOnManyToManyInverseDiagnostic);
    }

    // -------------------------------------------------------------------------
    // Negative tests — no diagnostic expected
    // -------------------------------------------------------------------------

    @Test
    public void testBidirectionalValidOneToManyInverseSide() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalDepartmentValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @OneToMany(mappedBy="department") with no @JoinTable — valid inverse side.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testBidirectionalValidOneToOneInverseSide() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalPersonValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // @OneToOne(mappedBy="resident") — valid inverse side, no diagnostic expected.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testBidirectionalUnidirectionalOneToMany() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalOrder.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Unidirectional @OneToMany — BidirectionalOrderItem has no back-reference.
        // No diagnostic expected.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testBidirectionalUnidirectionalManyToMany() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/bidirectional/BidirectionalProduct.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Unidirectional @ManyToMany — BidirectionalCategory has no back-reference.
        // No diagnostic expected.
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
