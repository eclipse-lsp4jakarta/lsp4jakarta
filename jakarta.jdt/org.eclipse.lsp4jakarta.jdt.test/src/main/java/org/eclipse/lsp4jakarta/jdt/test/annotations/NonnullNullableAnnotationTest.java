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
package org.eclipse.lsp4jakarta.jdt.test.annotations;

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
 * Tests the diagnostic for conflicting use of @Nonnull and @Nullable on the same element.
 */
public class NonnullNullableAnnotationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    private static final String MESSAGE = "The @Nonnull and @Nullable annotations are mutually exclusive and must not be used together on '%s'.";
    private static final String PRIMITIVE_MESSAGE = "The @Nullable annotation cannot be used on primitive types (boolean, byte, char, short, int, long, float, double, void).";

    @Test
    public void NonnullNullableConflict() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/annotations/NonnullNullableConflict.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Each diagnostic is reported on the @Nullable annotation.
        Diagnostic fieldConflict = d(9, 4, 13, String.format(MESSAGE, "conflictingField"), DiagnosticSeverity.Error,
                                     "jakarta-annotations", "NonnullNullableConflict");

        Diagnostic methodConflict = d(22, 4, 13, String.format(MESSAGE, "conflictingMethod"), DiagnosticSeverity.Error,
                                      "jakarta-annotations", "NonnullNullableConflict");

        Diagnostic paramConflict = d(34, 42, 51, String.format(MESSAGE, "p1"), DiagnosticSeverity.Error,
                                     "jakarta-annotations", "NonnullNullableConflict");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, fieldConflict, methodConflict, paramConflict);
    }

    @Test
    public void testNullableOnPrimitiveTypesInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/annotations/NullablePrimitiveTypeInvalid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // 8 Primitive fields
        Diagnostic dBooleanField = d(7, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dByteField = d(10, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dCharField = d(13, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dShortField = d(16, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dIntField = d(19, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dLongField = d(22, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dFloatField = d(25, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dDoubleField = d(28, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");

        // 8 Primitive return methods + 1 void return method
        Diagnostic dGetBooleanField = d(32, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dGetByteField = d(37, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dGetCharField = d(42, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dGetShortField = d(47, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dGetIntField = d(52, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dGetLongField = d(57, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dGetFloatField = d(62, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dGetDoubleField = d(67, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dGetVoidMethod = d(72, 4, 13, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");

        // 8 Primitive parameters
        Diagnostic dPBoolean = d(78, 12, 21, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dPByte = d(79, 12, 21, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dPChar = d(80, 12, 21, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dPShort = d(81, 12, 21, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dPInt = d(82, 12, 21, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dPLong = d(83, 12, 21, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dPFloat = d(84, 12, 21, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");
        Diagnostic dPDouble = d(85, 12, 21, PRIMITIVE_MESSAGE, DiagnosticSeverity.Error, "jakarta-annotations", "InvalidNullableOnPrimitiveType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              dBooleanField, dByteField, dCharField, dShortField, dIntField, dLongField, dFloatField, dDoubleField,
                              dGetBooleanField, dGetByteField, dGetCharField, dGetShortField, dGetIntField, dGetLongField, dGetFloatField, dGetDoubleField, dGetVoidMethod,
                              dPBoolean, dPByte, dPChar, dPShort, dPInt, dPLong, dPFloat, dPDouble);
    }

    @Test
    public void testNullableOnPrimitiveTypesValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/annotations/NullablePrimitiveTypeValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
