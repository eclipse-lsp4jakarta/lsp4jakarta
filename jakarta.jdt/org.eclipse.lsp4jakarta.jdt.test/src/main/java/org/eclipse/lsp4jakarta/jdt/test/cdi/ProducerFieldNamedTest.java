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
 * Tests for CDI producer fields with @Named annotation diagnostic.
 *
 */
public class ProducerFieldNamedTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void producerFieldWithNamedAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ProducerFieldWithNamed.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostics for producer fields with @Named annotation
        // Line 12: @Named("config") annotation on producer field config
        Diagnostic namedConfigDiagnostic = d(12, 4, 20,
                                             "Producer field 'config' must not declare a bean name using @Named annotation.",
                                             DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerFieldWithNamedAnnotation");

        // Line 17: @Named annotation (no value) on producer field greeting
        Diagnostic namedGreetingDiagnostic = d(17, 4, 10,
                                               "Producer field 'greeting' must not declare a bean name using @Named annotation.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidProducerFieldWithNamedAnnotation");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, namedConfigDiagnostic, namedGreetingDiagnostic);

        // Test code action for namedConfigDiagnostic - Remove @Named("config")
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, namedConfigDiagnostic);
        TextEdit removeNamedConfigEdit = te(12, 4, 13, 4, "");
        CodeAction removeNamedConfigAction = ca(uri, "Remove @Named", namedConfigDiagnostic, removeNamedConfigEdit);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, removeNamedConfigAction);

        // Test code action for namedGreetingDiagnostic - Remove @Named
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, namedGreetingDiagnostic);
        TextEdit removeNamedGreetingEdit = te(17, 4, 18, 4, "");
        CodeAction removeNamedGreetingAction = ca(uri, "Remove @Named", namedGreetingDiagnostic, removeNamedGreetingEdit);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, removeNamedGreetingAction);
    }
}