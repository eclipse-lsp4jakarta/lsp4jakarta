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
 * Tests for validation implemented in {@code PersistenceMappingDiagnosticsParticipant}.
 *
 */
public class PersistenceMappingDiagnosticsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -----------------------------------------------------------------------
    // @AttributeOverride — Valid cases
    // -----------------------------------------------------------------------

    /**
     * Valid: @AttributeOverride on @Embedded field with name="city" which exists in Address.
     */
    @Test
    public void validEmbeddableOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/ValidEmbeddableOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: class-level @AttributeOverride with name="address" which exists in Person.
     */
    @Test
    public void validSuperclassOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/ValidSuperclassOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: @AttributeOverrides container — both "address" and "id" exist in Person.
     */
    @Test
    public void validContainerOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/ValidContainerOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: name="salary" resolves to NamedPerson.salary (depth-2 MappedSuperclass chain).
     */
    @Test
    public void validDeepChainOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/ValidDeepChainOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: dot-notation name="zipcode.zip" — "zipcode" in AddressWithZipcode, "zip" in Zipcode.
     */
    @Test
    public void validDotNotationOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/ValidDotNotationOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: name="value.city" on Map @ElementCollection — "value." prefix present,
     * "city" exists in value type Address.
     */
    @Test
    public void validMapValueOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/ValidMapValueOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    // -----------------------------------------------------------------------
    // @AttributeOverride - Invalid cases
    // -----------------------------------------------------------------------

    /**
     * Invalid: name="zipcode" does not exist in Address (only "street" and "city").
     * Diagnostic fires on the @AttributeOverride annotation.
     */
    @Test
    public void invalidEmbeddableOverride_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidEmbeddableOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 28 (0-based): @AttributeOverride(name = "zipcode", column = @Column(name = "ADDR_ZIP"))
        Diagnostic zipcodeNotInAddress = d(27, 4, 77,
                                           "The name \"zipcode\" in @AttributeOverride does not match any declared field or property in \"Address\".",
                                           DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAttributeOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, zipcodeNotInAddress);
    }

    /**
     * Invalid: name="salary" does not exist in Person (only "id" and "address").
     * Diagnostic fires on the class-level @AttributeOverride annotation.
     */
    @Test
    public void invalidSuperclassOverride_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidSuperclassOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 21 (0-based): @AttributeOverride(name = "salary", column = @Column(name = "EMP_SALARY"))
        Diagnostic salaryNotInPerson = d(20, 0, 74,
                                         "The name \"salary\" in @AttributeOverride does not match any declared field or property in \"Person\".",
                                         DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAttributeOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, salaryNotInPerson);
    }

    /**
     * Invalid: @AttributeOverrides container with "id" (valid) and "bonus" (invalid — not in Person).
     * Diagnostic fires on the "bonus" entry only.
     */
    @Test
    public void invalidContainerOneEntry_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidContainerOneEntry.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 24 (0-based): @AttributeOverride(name = "bonus", column = @Column(name = "MGR_BONUS"))
        Diagnostic bonusNotInPerson = d(23, 4, 76,
                                        "The name \"bonus\" in @AttributeOverride does not match any declared field or property in \"Person\".",
                                        DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAttributeOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, bonusNotInPerson);
    }

    /**
     * Invalid: dot-notation "zipcode.postcode" — first segment "zipcode" exists in
     * AddressWithZipcode, but second segment "postcode" does not exist in Zipcode.
     */
    @Test
    public void invalidDotNotationSecondSegment_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidDotNotationSecondSegment.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 29 (0-based): @AttributeOverride(name = "zipcode.postcode", ...)
        Diagnostic postcodeNotInZipcode = d(28, 4, 85,
                                            "The name \"zipcode.postcode\" in @AttributeOverride cannot be resolved: \"postcode\" does not exist in \"Zipcode\".",
                                            DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAttributeOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, postcodeNotInZipcode);
    }

    /**
     * Invalid: dot-notation "location.zip" — first segment "location" does not exist
     * in AddressWithZipcode at all.
     */
    @Test
    public void invalidDotNotationFirstSegment_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidDotNotationFirstSegment.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 29 (0-based): @AttributeOverride(name = "location.zip", ...)
        Diagnostic locationNotInAddressWithZipcode = d(28, 4, 81,
                                                       "The name \"location.zip\" in @AttributeOverride cannot be resolved: \"location\" does not exist in \"AddressWithZipcode\".",
                                                       DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAttributeOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, locationNotInAddressWithZipcode);
    }

    /**
     * Invalid: name="city" on Map @ElementCollection without "key." or "value." prefix.
     */
    @Test
    public void invalidMapMissingPrefix_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidMapMissingPrefix.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 29 (0-based): @AttributeOverride(name = "city", column = @Column(name = "PROP_CITY"))
        Diagnostic cityMissingMapPrefix = d(28, 4, 75,
                                            "The name \"city\" in @AttributeOverride on a Map @ElementCollection must be prefixed with \"key.\" or \"value.\".",
                                            DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAttributeOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, cityMissingMapPrefix);
    }

    // -----------------------------------------------------------------------
    // @AssociationOverride — valid cases
    // -----------------------------------------------------------------------

    /**
     * Valid: @AssociationOverride on @Embedded field with name="manager" which exists in Department.
     */
    @Test
    public void validAssociationEmbeddableOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/ValidEmbeddableOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: class-level @AssociationOverride with name="supervisor" which exists in Person.
     */
    @Test
    public void validAssociationSuperclassOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/ValidSuperclassOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: @AssociationOverrides container — both "supervisor" and "id" exist in Person.
     */
    @Test
    public void validAssociationContainerOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/ValidContainerOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: name="teamLead" resolves to NamedPerson.teamLead (depth-2 MappedSuperclass chain).
     */
    @Test
    public void validAssociationDeepChainOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/ValidDeepChainOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Valid: dot-notation name="subDept.coordinator" — "subDept" in DepartmentWithTeam,
     * "coordinator" in SubTeam.
     */
    @Test
    public void validAssociationDotNotationOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/ValidDotNotationOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    // -----------------------------------------------------------------------
    // @AssociationOverride — invalid cases
    // -----------------------------------------------------------------------

    /**
     * Invalid: name="director" does not exist in Department (only "manager" and "lead").
     * Diagnostic fires on the @AssociationOverride annotation.
     */
    @Test
    public void invalidAssociationEmbeddableOverride_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/InvalidEmbeddableOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 28 (0-based 27): @AssociationOverride(name = "director", joinColumns = @JoinColumn(name = "DIR_ID"))
        Diagnostic directorNotInDepartment = d(27, 4, 87,
                                               "The name \"director\" in @AssociationOverride does not match any declared field or property in \"Department\".",
                                               DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAssociationOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, directorNotInDepartment);
    }

    /**
     * Invalid: name="mentor" does not exist in Person (only "supervisor" and "id").
     * Diagnostic fires on the class-level @AssociationOverride annotation.
     */
    @Test
    public void invalidAssociationSuperclassOverride_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/InvalidSuperclassOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 21 (0-based 20): @AssociationOverride(name = "mentor", joinColumns = @JoinColumn(name = "MENTOR_ID"))
        Diagnostic mentorNotInPerson = d(20, 0, 84,
                                         "The name \"mentor\" in @AssociationOverride does not match any declared field or property in \"Person\".",
                                         DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAssociationOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, mentorNotInPerson);
    }

    /**
     * Invalid: @AssociationOverrides container with "supervisor" (valid) and "mentor" (invalid — not in Person).
     * Diagnostic fires on the "mentor" entry only.
     */
    @Test
    public void invalidAssociationContainerOneEntry_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/InvalidContainerOneEntry.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 24 (0-based 23): @AssociationOverride(name = "mentor", joinColumns = @JoinColumn(name = "MENTOR_ID"))
        Diagnostic mentorNotInPerson = d(23, 4, 88,
                                         "The name \"mentor\" in @AssociationOverride does not match any declared field or property in \"Person\".",
                                         DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAssociationOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, mentorNotInPerson);
    }

    /**
     * Invalid: dot-notation "subDept.owner" — first segment "subDept" exists in
     * DepartmentWithTeam, but second segment "owner" does not exist in SubTeam.
     */
    @Test
    public void invalidAssociationDotNotationSecondSegment_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/InvalidDotNotationSecondSegment.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 29 (0-based 28): @AssociationOverride(name = "subDept.owner", joinColumns = @JoinColumn(name = "OWNER_ID"))
        Diagnostic ownerNotInSubTeam = d(28, 4, 94,
                                         "The name \"subDept.owner\" in @AssociationOverride cannot be resolved: \"owner\" does not exist in \"SubTeam\".",
                                         DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAssociationOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, ownerNotInSubTeam);
    }

    /**
     * Invalid: dot-notation "division.coordinator" — first segment "division" does not exist
     * in DepartmentWithTeam at all.
     */
    @Test
    public void invalidAssociationDotNotationFirstSegment_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/InvalidDotNotationFirstSegment.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 29 (0-based 28): @AssociationOverride(name = "division.coordinator", joinColumns = @JoinColumn(name = "COORD_ID"))
        Diagnostic divisionNotInDepartmentWithTeam = d(28, 4, 101,
                                                       "The name \"division.coordinator\" in @AssociationOverride cannot be resolved: \"division\" does not exist in \"DepartmentWithTeam\".",
                                                       DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAssociationOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, divisionNotInDepartmentWithTeam);
    }

    // -----------------------------------------------------------------------
    // @AttributeOverride — method-level (property-based access)
    // -----------------------------------------------------------------------

    /**
     * Valid: @AttributeOverride on a property-based getter with name="city" which exists in Address.
     */
    @Test
    public void validPropertyBasedAttributeOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/ValidPropertyBasedOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Invalid: @AttributeOverride on a property-based getter with name="zipcode" which does
     * not exist in Address (only "street" and "city").
     * Diagnostic fires on the @AttributeOverride annotation on the getter.
     */
    @Test
    public void invalidPropertyBasedAttributeOverride_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidPropertyBasedOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 30 (0-based 29): @AttributeOverride(name = "zipcode", column = @Column(name = "ADDR_ZIP"))
        Diagnostic zipcodeNotInAddress = d(29, 4, 77,
                                           "The name \"zipcode\" in @AttributeOverride does not match any declared field or property in \"Address\".",
                                           DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAttributeOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, zipcodeNotInAddress);
    }

    // -----------------------------------------------------------------------
    // @AssociationOverride — method-level (property-based access)
    // -----------------------------------------------------------------------

    /**
     * Valid: @AssociationOverride on a property-based getter with name="manager" which exists in Department.
     */
    @Test
    public void validPropertyBasedAssociationOverride_nodiagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/ValidPropertyBasedOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS /* no diagnostics expected */);
    }

    /**
     * Invalid: @AssociationOverride on a property-based getter with name="director" which does
     * not exist in Department (only "manager" and "lead").
     * Diagnostic fires on the @AssociationOverride annotation on the getter.
     */
    @Test
    public void invalidPropertyBasedAssociationOverride_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/associationoverride/InvalidPropertyBasedOverride.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 30 (0-based 29): @AssociationOverride(name = "director", joinColumns = @JoinColumn(name = "DIR_ID"))
        Diagnostic directorNotInDepartment = d(29, 4, 87,
                                               "The name \"director\" in @AssociationOverride does not match any declared field or property in \"Department\".",
                                               DiagnosticSeverity.Error, "jakarta-persistence", "InvalidAssociationOverrideName");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, directorNotInDepartment);
    }

    // -----------------------------------------------------------------------
    // @AttributeOverride on non-embedded field/property (new diagnostic + quickfix)
    // -----------------------------------------------------------------------

    /**
     * Invalid: @AttributeOverride on a plain String field that is not annotated
     * with @Embedded, @EmbeddedId, or @ElementCollection.
     * Expected: diagnostic AttributeOverrideOnNonEmbeddedField and quickfix to remove the annotation.
     */
    @Test
    public void attributeOverrideOnPlainField_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidAttributeOverrideOnPlainField.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 27 (0-based): @AttributeOverride(name = "city", column = @Column(name = "EMP_CITY"))
        Diagnostic overrideOnPlainField = d(26, 4, 74,
                                            "@AttributeOverride is only valid on a field or property annotated with @Embedded, @EmbeddedId or @ElementCollection.",
                                            DiagnosticSeverity.Error, "jakarta-persistence", "AttributeOverrideOnNonEmbeddedField");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, overrideOnPlainField);

        // Quickfix: only @AttributeOverride is present → one action with its simple name
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, overrideOnPlainField);
        TextEdit removeAttributeOverride = te(26, 4, 27, 4, "");
        CodeAction removeAttributeOverrideAction = ca(uri, "Remove @AttributeOverride", overrideOnPlainField, removeAttributeOverride);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAttributeOverrideAction);
    }

    /**
     * Invalid: @AttributeOverrides container on a plain Long @Id field that is not
     * annotated with @Embedded, @EmbeddedId, or @ElementCollection.
     * Expected: diagnostic AttributeOverrideOnNonEmbeddedField and quickfix to remove the container annotation.
     */
    @Test
    public void attributeOverrideContainerOnIdField_diagnostic() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/persistence/attributeoverride/InvalidAttributeOverrideOnIdField.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Line 26 (0-based): @AttributeOverrides({
        Diagnostic overrideContainerOnIdField = d(25, 4, 27, 6,
                                                  "@AttributeOverrides is only valid on a field or property annotated with @Embedded, @EmbeddedId or @ElementCollection.",
                                                  DiagnosticSeverity.Error, "jakarta-persistence", "AttributeOverrideOnNonEmbeddedField");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, overrideContainerOnIdField);

        // Quickfix: only @AttributeOverrides is present → one action with its simple name
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, overrideContainerOnIdField);
        TextEdit removeAttributeOverrides = te(25, 4, 28, 4, "");
        CodeAction removeAttributeOverridesAction = ca(uri, "Remove @AttributeOverrides", overrideContainerOnIdField, removeAttributeOverrides);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeAttributeOverridesAction);
    }
}
