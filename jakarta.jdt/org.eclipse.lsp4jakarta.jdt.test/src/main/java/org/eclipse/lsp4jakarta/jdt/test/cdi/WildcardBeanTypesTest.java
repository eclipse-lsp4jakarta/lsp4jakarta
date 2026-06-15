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

package org.eclipse.lsp4jakarta.jdt.test.cdi;

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
 * Tests for CDI wildcard bean type diagnostics.
 *
 * According to CDI specification section 2.2.1:
 * "A parameterized type that contains a wildcard type parameter is not a legal bean type."
 */
public class WildcardBeanTypesTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void wildcardBeanTypes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/WildcardBeanTypes.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostics for wildcard types in @Inject fields
        Diagnostic injectWildcard = d(26, 12, 24,
                                      "Wildcard types are not legal bean types. Injection points must use concrete parameterized types without wildcards (?, ? extends, ? super).",
                                      DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInInjectField");

        Diagnostic injectExtendsWildcard = d(30, 27, 46,
                                             "Wildcard types are not legal bean types. Injection points must use concrete parameterized types without wildcards (?, ? extends, ? super).",
                                             DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInInjectField");

        Diagnostic injectSuperWildcard = d(34, 26, 43,
                                           "Wildcard types are not legal bean types. Injection points must use concrete parameterized types without wildcards (?, ? extends, ? super).",
                                           DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInInjectField");

        Diagnostic injectMapWildcard = d(38, 19, 30,
                                         "Wildcard types are not legal bean types. Injection points must use concrete parameterized types without wildcards (?, ? extends, ? super).",
                                         DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInInjectField");

        // Test expected diagnostics for wildcard types in @Produces fields
        Diagnostic producerFieldWildcard = d(50, 12, 32,
                                             "Wildcard types are not legal bean types. Producer fields must use concrete parameterized types without wildcards (?, ? extends, ? super).",
                                             DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInProducerField");

        Diagnostic producerFieldExtendsWildcard = d(54, 27, 54,
                                                    "Wildcard types are not legal bean types. Producer fields must use concrete parameterized types without wildcards (?, ? extends, ? super).",
                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInProducerField");

        // Test expected diagnostics for wildcard types in @Produces methods
        Diagnostic producerMethodMapWildcard = d(62, 26, 44,
                                                 "Wildcard types are not legal bean types. Producer methods must return concrete parameterized types without wildcards (?, ? extends, ? super).",
                                                 DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInProducerMethod");

        Diagnostic producerMethodExtendsWildcard = d(68, 34, 60,
                                                     "Wildcard types are not legal bean types. Producer methods must return concrete parameterized types without wildcards (?, ? extends, ? super).",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInProducerMethod");

        Diagnostic producerMethodSuperWildcard = d(74, 33, 57,
                                                   "Wildcard types are not legal bean types. Producer methods must return concrete parameterized types without wildcards (?, ? extends, ? super).",
                                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInProducerMethod");

        Diagnostic producerMethodArrayWildcard = d(80, 21, 41,
                                                   "Wildcard types are not legal bean types. Producer methods must return concrete parameterized types without wildcards (?, ? extends, ? super).",
                                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidWildcardTypeInProducerMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              injectWildcard, injectExtendsWildcard, injectSuperWildcard, injectMapWildcard,
                              producerFieldWildcard, producerFieldExtendsWildcard,
                              producerMethodMapWildcard, producerMethodExtendsWildcard, producerMethodSuperWildcard, producerMethodArrayWildcard);
    }
}