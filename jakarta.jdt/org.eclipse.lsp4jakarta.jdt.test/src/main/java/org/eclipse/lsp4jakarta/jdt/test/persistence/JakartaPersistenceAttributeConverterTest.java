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
 * Tests for the PersistenceAttributeConverterDiagnosticsParticipant.
 *
 * Validates that the @Converter annotation must only be applied to classes
 * that implement jakarta.persistence.AttributeConverter.
 */
public class JakartaPersistenceAttributeConverterTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * A class annotated with @Converter that does not implement AttributeConverter
     * must produce a diagnostic.
     */
    @Test
    public void testConverterWithoutAttributeConverter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/ConverterWithoutAttributeConverter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic converterMissingInterface = d(6, 13, 47,
                                                  "A class annotated with @Converter must implement the jakarta.persistence.AttributeConverter interface.",
                                                  DiagnosticSeverity.Error, "jakarta-persistence", "ConverterMustImplementAttributeConverter");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, converterMissingInterface);
    }

    /**
     * An abstract class annotated with @Converter that does not implement
     * AttributeConverter must also produce a diagnostic.
     */
    @Test
    public void testConverterAbstractClassWithoutAttributeConverter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/ConverterAbstractClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic abstractConverterMissingInterface = d(6, 22, 44,
                                                         "A class annotated with @Converter must implement the jakarta.persistence.AttributeConverter interface.",
                                                         DiagnosticSeverity.Error, "jakarta-persistence", "ConverterMustImplementAttributeConverter");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, abstractConverterMissingInterface);
    }

    /**
     * A class annotated with @Converter that correctly implements AttributeConverter
     * must not produce any diagnostics.
     */
    @Test
    public void testConverterWithAttributeConverterValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/ConverterWithAttributeConverter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Verify that NO diagnostics are produced for a valid @Converter implementation
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

}
