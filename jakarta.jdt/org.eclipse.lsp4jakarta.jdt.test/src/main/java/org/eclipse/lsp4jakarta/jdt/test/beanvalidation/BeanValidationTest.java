/*******************************************************************************
* Copyright (c) 2021, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.test.beanvalidation;

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

public class BeanValidationTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void validFieldConstraints() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/beanvalidation/ValidConstraints.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // should be no errors
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void fieldConstraintValidation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/beanvalidation/FieldConstraintValidation.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics
        Diagnostic d1 = d(10, 16, 23,
                          "The @AssertTrue annotation can only be used on boolean and Boolean type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonBooleanMethodOrField",
                          "jakarta.validation.constraints.AssertTrue");
        Diagnostic d2 = d(13, 19, 24,
                          "The @AssertFalse annotation can only be used on boolean and Boolean type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonBooleanMethodOrField",
                          "jakarta.validation.constraints.AssertFalse");
        Diagnostic d3 = d(17, 19, 29,
                          "The @DecimalMax annotation can only be used on: \n"
                                      + "- BigDecimal \n"
                                      + "- BigInteger \n"
                                      + "- CharSequence\n"
                                      + "- byte, short, int, long (and their respective wrappers) \n"
                                      + " type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation",
                          "InvalidAnnotationOnNonBigDecimalCharByteShortIntLongMethodOrField", "jakarta.validation.constraints.DecimalMax");
        Diagnostic d4 = d(17, 19, 29,
                          "The @DecimalMin annotation can only be used on: \n"
                                      + "- BigDecimal \n"
                                      + "- BigInteger \n"
                                      + "- CharSequence\n"
                                      + "- byte, short, int, long (and their respective wrappers) \n"
                                      + " type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation",
                          "InvalidAnnotationOnNonBigDecimalCharByteShortIntLongMethodOrField", "jakarta.validation.constraints.DecimalMin");
        Diagnostic d5 = d(20, 20, 26,
                          "The @Digits annotation can only be used on: \n"
                                      + "- BigDecimal \n"
                                      + "- BigInteger \n"
                                      + "- CharSequence\n"
                                      + "- byte, short, int, long (and their respective wrappers) \n"
                                      + " type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation",
                          "InvalidAnnotationOnNonBigDecimalCharByteShortIntLongMethodOrField", "jakarta.validation.constraints.Digits");
        Diagnostic d6 = d(23, 20, 32,
                          "The @Email annotation can only be used on String and CharSequence type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonStringMethodOrField",
                          "jakarta.validation.constraints.Email");
        Diagnostic d7 = d(26, 20, 34,
                          "The @FutureOrPresent annotation can only be used on: Date, Calendar, Instant, LocalDate, LocalDateTime, LocalTime, MonthDay, OffsetDateTime, OffsetTime, Year, YearMonth, ZonedDateTime, HijrahDate, JapaneseDate, JapaneseDate, MinguoDate and ThaiBuddhistDate type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonDateTimeMethodOrField",
                          "jakarta.validation.constraints.FutureOrPresent");
        Diagnostic d8 = d(29, 19, 30,
                          "The @Future annotation can only be used on: Date, Calendar, Instant, LocalDate, LocalDateTime, LocalTime, MonthDay, OffsetDateTime, OffsetTime, Year, YearMonth, ZonedDateTime, HijrahDate, JapaneseDate, JapaneseDate, MinguoDate and ThaiBuddhistDate type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonDateTimeMethodOrField",
                          "jakarta.validation.constraints.Future");
        Diagnostic d9 = d(33, 20, 23,
                          "The @Min annotation can only be used on \n"
                                      + "- BigDecimal \n"
                                      + "- BigInteger\n"
                                      + "- byte, short, int, long (and their respective wrappers) \n"
                                      + " type fields.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation",
                          "InvalidAnnotationOnNonMinMaxMethodOrField", "jakarta.validation.constraints.Min");
        Diagnostic d10 = d(33, 20, 23,
                           "The @Max annotation can only be used on \n"
                                       + "- BigDecimal \n"
                                       + "- BigInteger\n"
                                       + "- byte, short, int, long (and their respective wrappers) \n"
                                       + " type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation",
                           "InvalidAnnotationOnNonMinMaxMethodOrField", "jakarta.validation.constraints.Max");
        Diagnostic d11 = d(36, 20, 27,
                           "The @Negative annotation can only be used on \n"
                                       + "- BigDecimal \n"
                                       + "- BigInteger\n"
                                       + "- byte, short, int, long, float, double (and their respective wrappers) \n"
                                       + " type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation",
                           "InvalidAnnotationOnNonPositiveMethodOrField", "jakarta.validation.constraints.Negative");
        Diagnostic d12 = d(39, 19, 25,
                           "The @NegativeOrZero annotation can only be used on \n"
                                       + "- BigDecimal \n"
                                       + "- BigInteger\n"
                                       + "- byte, short, int, long, float, double (and their respective wrappers) \n"
                                       + " type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation",
                           "InvalidAnnotationOnNonPositiveMethodOrField", "jakarta.validation.constraints.NegativeOrZero");
        Diagnostic d13 = d(42, 20, 32,
                           "The @NotBlank annotation can only be used on String and CharSequence type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonStringMethodOrField",
                           "jakarta.validation.constraints.NotBlank");
        Diagnostic d14 = d(45, 21, 31,
                           "The @Pattern annotation can only be used on String and CharSequence type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonStringMethodOrField",
                           "jakarta.validation.constraints.Pattern");
        Diagnostic d15 = d(48, 19, 33,
                           "The @Past annotation can only be used on: Date, Calendar, Instant, LocalDate, LocalDateTime, LocalTime, MonthDay, OffsetDateTime, OffsetTime, Year, YearMonth, ZonedDateTime, HijrahDate, JapaneseDate, JapaneseDate, MinguoDate and ThaiBuddhistDate type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonDateTimeMethodOrField",
                           "jakarta.validation.constraints.Past");
        Diagnostic d16 = d(51, 19, 33,
                           "The @PastOrPresent annotation can only be used on: Date, Calendar, Instant, LocalDate, LocalDateTime, LocalTime, MonthDay, OffsetDateTime, OffsetTime, Year, YearMonth, ZonedDateTime, HijrahDate, JapaneseDate, JapaneseDate, MinguoDate and ThaiBuddhistDate type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonDateTimeMethodOrField",
                           "jakarta.validation.constraints.PastOrPresent");
        Diagnostic d17 = d(54, 21, 25,
                           "The @Positive annotation can only be used on \n"
                                       + "- BigDecimal \n"
                                       + "- BigInteger\n"
                                       + "- byte, short, int, long, float, double (and their respective wrappers) \n"
                                       + " type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonPositiveMethodOrField",
                           "jakarta.validation.constraints.Positive");
        Diagnostic d18 = d(57, 25, 34,
                           "The @PositiveOrZero annotation can only be used on \n"
                                       + "- BigDecimal \n"
                                       + "- BigInteger\n"
                                       + "- byte, short, int, long, float, double (and their respective wrappers) \n"
                                       + " type fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonPositiveMethodOrField",
                           "jakarta.validation.constraints.PositiveOrZero");
        Diagnostic d19 = d(60, 27, 36,
                           "Constraint annotations are not allowed on static fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidConstrainAnnotationOnStaticMethodOrField",
                           "jakarta.validation.constraints.AssertTrue");
        Diagnostic d20 = d(63, 27, 36,
                           "Constraint annotations are not allowed on static fields.",
                           DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidConstrainAnnotationOnStaticMethodOrField",
                           "jakarta.validation.constraints.Past");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6, d7, d8,
                              d9, d10, d11, d12, d13, d14, d15, d16, d17, d18, d19, d20);

        // Test quickfix codeActions - type (1-17), static, static+type (should only
        // display static)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te = te(9, 4, 10, 4, "");
        CodeAction ca = ca(uri, "Remove constraint annotation AssertTrue from element", d1, te);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca);
        
        JakartaJavaCodeActionParams codeActionParams5 = createCodeActionParams(uri, d2);
        TextEdit te6 = te(12, 4, 13, 4, "");
        CodeAction ca6 = ca(uri, "Remove constraint annotation AssertFalse from element", d2, te6);

        assertJavaCodeAction(codeActionParams5, IJDT_UTILS, ca6);

        JakartaJavaCodeActionParams codeActionParams6 = createCodeActionParams(uri, d3);
        TextEdit te7 = te(15, 4, 16, 4, "");
        CodeAction ca7 = ca(uri, "Remove constraint annotation DecimalMax from element", d3, te7);

        assertJavaCodeAction(codeActionParams6, IJDT_UTILS, ca7);

        JakartaJavaCodeActionParams codeActionParams7 = createCodeActionParams(uri, d4);
        TextEdit te8 = te(16, 4, 17, 4, "");
        CodeAction ca8 = ca(uri, "Remove constraint annotation DecimalMin from element", d3, te8);

        assertJavaCodeAction(codeActionParams7, IJDT_UTILS, ca8);

        JakartaJavaCodeActionParams codeActionParams8 = createCodeActionParams(uri, d5);
        TextEdit te9 = te(19, 4, 20, 4, "");
        CodeAction ca9 = ca(uri, "Remove constraint annotation Digits from element", d5, te9);

        assertJavaCodeAction(codeActionParams8, IJDT_UTILS, ca9);
        
        JakartaJavaCodeActionParams codeActionParams12 = createCodeActionParams(uri, d9);
        TextEdit te13 = te(31, 4, 32, 4, "");
        CodeAction ca13 = ca(uri, "Remove constraint annotation Min from element", d9, te13);
        
        assertJavaCodeAction(codeActionParams12, IJDT_UTILS, ca13);
        
        JakartaJavaCodeActionParams codeActionParams13 = createCodeActionParams(uri, d10);
        TextEdit te14 = te(32, 4, 33, 4, "");
        CodeAction ca14 = ca(uri, "Remove constraint annotation Max from element", d10, te14);
        
        assertJavaCodeAction(codeActionParams13, IJDT_UTILS, ca14);
        
        JakartaJavaCodeActionParams codeActionParams14 = createCodeActionParams(uri, d11);
        TextEdit te15 = te(35, 4, 36, 4, "");
        CodeAction ca15 = ca(uri, "Remove constraint annotation Negative from element", d11, te15);
        
        assertJavaCodeAction(codeActionParams14, IJDT_UTILS, ca15);
        
        JakartaJavaCodeActionParams codeActionParams15 = createCodeActionParams(uri, d12);
        TextEdit te16 = te(38, 4, 39, 4, "");
        CodeAction ca16 = ca(uri, "Remove constraint annotation NegativeOrZero from element", d12, te16);
        
        assertJavaCodeAction(codeActionParams15, IJDT_UTILS, ca16);
        
        JakartaJavaCodeActionParams codeActionParams16 = createCodeActionParams(uri, d13);
        TextEdit te17 = te(41, 4, 42, 4, "");
        CodeAction ca17 = ca(uri, "Remove constraint annotation NotBlank from element", d13, te17);
        
        assertJavaCodeAction(codeActionParams16, IJDT_UTILS, ca17);
        
        JakartaJavaCodeActionParams codeActionParams17 = createCodeActionParams(uri, d14);
        TextEdit te18 = te(44, 4, 45, 4, "");
        CodeAction ca18 = ca(uri, "Remove constraint annotation Pattern from element", d14, te18);
        
        assertJavaCodeAction(codeActionParams17, IJDT_UTILS, ca18);

        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d19);
        TextEdit te1 = te(59, 4, 60, 4, "");
        TextEdit te2 = te(60, 11, 60, 18, "");
        CodeAction ca1 = ca(uri, "Remove constraint annotation AssertTrue from element", d19, te1);
        CodeAction ca2 = ca(uri, "Remove the 'static' modifier", d19, te2);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca1, ca2);

        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d20);
        TextEdit te3 = te(62, 4, 63, 4, "");
        TextEdit te4 = te(63, 11, 63, 18, "");
        CodeAction ca3 = ca(uri, "Remove constraint annotation Past from element", d20, te3);
        CodeAction ca4 = ca(uri, "Remove the 'static' modifier", d20, te4);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca3, ca4);

        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, d18);
        TextEdit te5 = te(56, 4, 57, 4, "");
        CodeAction ca5 = ca(uri, "Remove constraint annotation PositiveOrZero from element", d18, te5);

        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, ca5);
    }

    @Test
    public void methodConstraintValidation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/beanvalidation/MethodConstraintValidation.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostics
        Diagnostic d1 = d(20, 26, 38,
                          "Constraint annotations are not allowed on static methods.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidConstrainAnnotationOnStaticMethodOrField",
                          "jakarta.validation.constraints.AssertTrue");
        Diagnostic d2 = d(25, 18, 28,
                          "The @AssertTrue annotation can only be used on boolean and Boolean type methods.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidAnnotationOnNonBooleanMethodOrField",
                          "jakarta.validation.constraints.AssertTrue");
        Diagnostic d3 = d(30, 23, 33,
                          "Constraint annotations are not allowed on static methods.",
                          DiagnosticSeverity.Error, "jakarta-bean-validation", "InvalidConstrainAnnotationOnStaticMethodOrField",
                          "jakarta.validation.constraints.AssertFalse");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);

        // Test quickfix codeActions
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d1);
        TextEdit te = te(19, 4, 20, 4, "");
        TextEdit te2 = te(20, 10, 20, 17, "");
        CodeAction ca = ca(uri, "Remove constraint annotation AssertTrue from element", d1, te);
        CodeAction ca2 = ca(uri, "Remove the 'static' modifier", d1, te2);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca, ca2);

        codeActionParams = createCodeActionParams(uri, d2);
        te = te(24, 4, 25, 4, "");
        ca = ca(uri, "Remove constraint annotation AssertTrue from element", d2, te);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca);

        codeActionParams = createCodeActionParams(uri, d3);
        te = te(19, 4, 20, 4, "");
        te2 = te(20, 10, 20, 17, "");
        ca = ca(uri, "Remove constraint annotation AssertFalse from element", d3, te);
        ca2 = ca(uri, "Remove the 'static' modifier", d3, te2);
    }
}
