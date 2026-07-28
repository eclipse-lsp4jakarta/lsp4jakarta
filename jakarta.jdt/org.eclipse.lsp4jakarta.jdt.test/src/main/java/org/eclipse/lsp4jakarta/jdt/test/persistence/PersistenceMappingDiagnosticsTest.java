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
 * Tests for validation implemented in {@code PersistenceMappingDiagnosticsParticipant}.
 *
 */
public class PersistenceMappingDiagnosticsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -----------------------------------------------------------------------
    // Valid cases — no diagnostic expected
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
    // Invalid cases — diagnostic expected
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
}
