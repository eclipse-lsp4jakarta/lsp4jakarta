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

public class MultipleObserverParamsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void multipleObserverParams() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/MultipleObserverParams.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Invalid: Two parameters, each with @Observes
        Diagnostic d1 = d(18, 16, 34,
                          "A method cannot have more than one parameter annotated with @Observes or @ObservesAsync. Found parameters: event1, event2.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        // Invalid: Two parameters, each with @ObservesAsync
        Diagnostic d2 = d(23, 16, 39,
                          "A method cannot have more than one parameter annotated with @Observes or @ObservesAsync. Found parameters: event1, event2.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        // Invalid: One parameter with @Observes, another with @ObservesAsync
        Diagnostic d3 = d(28, 16, 47,
                          "A method cannot have more than one parameter annotated with @Observes or @ObservesAsync. Found parameters: event1, event2.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        // Invalid: Three parameters with observer annotations
        Diagnostic d4 = d(33, 16, 37,
                          "A method cannot have more than one parameter annotated with @Observes or @ObservesAsync. Found parameters: event1, event2, event3.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        // Invalid: One parameter with both @Observes and @ObservesAsync (existing test case)
        Diagnostic d5 = d(38, 16, 38,
                          "A CDI method must not have parameter(s): event annotated with @Observes and @ObservesAsync.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidObservesObservesAsyncMethodParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5);
    }
}

// Made with Bob
