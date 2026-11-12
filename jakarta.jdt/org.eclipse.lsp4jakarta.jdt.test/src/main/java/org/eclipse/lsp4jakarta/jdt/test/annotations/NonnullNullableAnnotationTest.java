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
}
