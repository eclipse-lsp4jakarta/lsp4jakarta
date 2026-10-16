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
 * Tests for {@link org.eclipse.lsp4jakarta.jdt.internal.persistence.DerivedIdentityDiagnosticsParticipant}
 * covering the derived-identity rules of Jakarta Persistence 3.0 §2.4.1 and §11.1.20.
 */
public class DerivedIdentityDiagnosticsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    private static final String PKG = "src/main/java/io/openliberty/sample/jakarta/persistence/derivedidentity/";

    private JakartaJavaDiagnosticsParams paramsFor(String className) throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path(PKG + className + ".java"));
        JakartaJavaDiagnosticsParams params = new JakartaJavaDiagnosticsParams();
        params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
        return params;
    }

    private static Diagnostic error(int line, int start, int end, String message, String code) {
        return d(line, start, end, message, DiagnosticSeverity.Error, "jakarta-persistence", code);
    }

    // ---- Rule 1: @MapsId must be on @OneToOne / @ManyToOne ------------------------------------

    /** {@code @MapsId} on a plain {@code @Id} field: field "id" at 0-based line 15, cols 17-19. */
    @Test
    public void testMapsIdOnNonRelationshipField() throws Exception {
        assertJavaDiagnostics(paramsFor("MapsIdOnNonRelationshipField"), IJDT_UTILS,
                              error(15, 17, 19,
                                    "@MapsId must only be applied to a field or property also annotated with @OneToOne or @ManyToOne.",
                                    "MapsIdOnInvalidRelationship"));
    }

    // ---- Rule 2: @MapsId("value") requires a composite key ------------------------------------

    /** {@code @MapsId("empId")} with only a simple {@code @Id}: "employee" at line 19, cols 21-29. */
    @Test
    public void testMapsIdWithValueRequiresCompositeKey() throws Exception {
        assertJavaDiagnostics(paramsFor("MapsIdWithSimpleKey"), IJDT_UTILS,
                              error(19, 21, 29,
                                    "@MapsId that specifies a value can only be used on an entity that declares a composite primary key via @EmbeddedId or @IdClass.",
                                    "MapsIdRequiresCompositeKey"));
    }

    /** {@code @MapsId("empId")} on an entity with an {@code @IdClass} composite key is valid. */
    @Test
    public void testMapsIdWithValueAndIdClassIsValid() throws Exception {
        assertJavaDiagnostics(paramsFor("ValidMapsIdWithIdClass"), IJDT_UTILS);
    }

    /** Spec §2.4.1.1 / Example 4b: value-less {@code @MapsId} with a simple {@code @Id} is valid. */
    @Test
    public void testValuelessMapsIdWithSimpleKeyIsValid() throws Exception {
        assertJavaDiagnostics(paramsFor("ValidMapsIdSimpleKey"), IJDT_UTILS);
    }

    // ---- Rule 4: @MapsId value must name an attribute of the @EmbeddedId class ----------------

    /** {@code @MapsId("nonExistent")}: "employee" at line 19, cols 21-29. */
    @Test
    public void testMapsIdValueNotInEmbeddedIdClass() throws Exception {
        assertJavaDiagnostics(paramsFor("MapsIdValueNotInEmbeddedId"), IJDT_UTILS,
                              error(19, 21, 29,
                                    "The value 'nonExistent' specified in @MapsId does not match any attribute in the @EmbeddedId class 'ProjectId'.",
                                    "MapsIdValueNotInEmbeddedIdClass"));
    }

    // ---- Rule 5: attribute type must match the parent PK type ---------------------------------

    /** ProjectId.empId is long but Department's @Id is String: "department" at line 20, cols 23-33. */
    @Test
    public void testMapsIdValueTypeMismatchSimpleParent() throws Exception {
        assertJavaDiagnostics(paramsFor("MapsIdValueTypeMismatch"), IJDT_UTILS,
                              error(20, 23, 33,
                                    "The type of attribute 'empId' in the @EmbeddedId class 'ProjectId' ('long') is not compatible with the primary key type of the parent entity 'Department' ('java.lang.String').",
                                    "MapsIdValueTypeMismatch"));
    }

    /** Spec Example 3b: the parent has an @EmbeddedId, so the attribute must have that type. */
    @Test
    public void testMapsIdValueTypeMismatchCompositeParent() throws Exception {
        assertJavaDiagnostics(paramsFor("MapsIdCompositeParentTypeMismatch"), IJDT_UTILS,
                              error(20, 33, 39,
                                    "The type of attribute 'wrongKey' in the @EmbeddedId class 'ChildKey' ('java.lang.String') is not compatible with the primary key type of the parent entity 'ParentWithEmbeddedId' ('io.openliberty.sample.jakarta.persistence.derivedidentity.ProjectId').",
                                    "MapsIdValueTypeMismatch"));
    }

    /** Property-access style: GetterAccessKey.isActive() is boolean but Department's @Id is String: "department" at line 20, cols 23-33. */
    @Test
    public void testMapsIdValueTypeMismatchIsPropertyAccess() throws Exception {
        assertJavaDiagnostics(paramsFor("MapsIdIsPropertyAccessTypeMismatch"), IJDT_UTILS,
                              error(20, 23, 33,
                                    "The type of attribute 'active' in the @EmbeddedId class 'GetterAccessKey' ('boolean') is not compatible with the primary key type of the parent entity 'Department' ('java.lang.String').",
                                    "MapsIdValueTypeMismatch"));
    }

    // ---- Value-less @MapsId with @EmbeddedId (spec Examples 5b / 6b) --------------------------

    /** The whole embedded id (ChildKey) differs from the parent's PK (ProjectId): "parent" at line 20, cols 33-39. */
    @Test
    public void testValuelessMapsIdPrimaryKeyTypeMismatch() throws Exception {
        assertJavaDiagnostics(paramsFor("MapsIdValuelessPkTypeMismatch"), IJDT_UTILS,
                              error(20, 33, 39,
                                    "The primary key type of the parent entity ('io.openliberty.sample.jakarta.persistence.derivedidentity.ChildKey') must be same as the primary key type of the dependent entity 'ParentWithEmbeddedId' ('io.openliberty.sample.jakarta.persistence.derivedidentity.ProjectId') when @MapsId is specified without a value.",
                                    "MapsIdPrimaryKeyTypeMismatch"));
    }

    /** Simple @Id (String) differs from parent's simple PK (Employee.id: long): "employee" at line 20, cols 21-29. */
    @Test
    public void testValuelessMapsIdSimpleKeyTypeMismatch() throws Exception {
        assertJavaDiagnostics(paramsFor("MapsIdSimpleKeyTypeMismatch"), IJDT_UTILS,
                              error(20, 21, 29,
                                    "The primary key type of the parent entity ('java.lang.String') must be same as the primary key type of the dependent entity 'Employee' ('long') when @MapsId is specified without a value.",
                                    "MapsIdPrimaryKeyTypeMismatch"));
    }

    @Test
    public void testValuelessMapsIdWithMatchingEmbeddedIdIsValid() throws Exception {
        assertJavaDiagnostics(paramsFor("ValidMapsIdValuelessEmbeddedId"), IJDT_UTILS);
    }

    // ---- Valid cases --------------------------------------------------------------------------

    /** Spec Example 1b: {@code @MapsId("empId")} where ProjectId.empId (long) matches Employee's @Id (long). */
    @Test
    public void testValidMapsIdWithValue() throws Exception {
        assertJavaDiagnostics(paramsFor("ValidDerivedIdentityEmbeddedId"), IJDT_UTILS);
    }

    /** Spec Example 3b: the attribute has the parent's @EmbeddedId type. */
    @Test
    public void testValidMapsIdWithCompositeParent() throws Exception {
        assertJavaDiagnostics(paramsFor("ValidMapsIdCompositeParent"), IJDT_UTILS);
    }

    /** Property-access style with getEmpId() resolving long matching Employee's @Id (long). */
    @Test
    public void testValidMapsIdPropertyAccess() throws Exception {
        assertJavaDiagnostics(paramsFor("ValidMapsIdPropertyAccess"), IJDT_UTILS);
    }
}
