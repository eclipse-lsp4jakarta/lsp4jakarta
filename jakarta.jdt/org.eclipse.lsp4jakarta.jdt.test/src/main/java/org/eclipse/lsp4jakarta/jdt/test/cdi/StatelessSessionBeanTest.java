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

public class StatelessSessionBeanTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void statelessSessionBeanWithIllegalScope() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/StatelessSessionBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostics
        Diagnostic d1 = d(10, 13, 39,
                          "A stateless session bean belongs to the @Dependent pseudo-scope; any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanWithIllegalScope");

        Diagnostic d2 = d(16, 6, 32,
                          "A stateless session bean belongs to the @Dependent pseudo-scope; any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanWithIllegalScope");

        Diagnostic d3 = d(23, 6, 33,
                          "A stateless session bean belongs to the @Dependent pseudo-scope; any other scope is invalid.",
                          DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanWithIllegalScope");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);

        // Assert for diagnostic d1 - Two quickfixes: Remove @Stateless or Replace scope with @Dependent
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, d1);
        TextEdit te1a = te(8, 0, 9, 0, "");
        CodeAction ca1a = ca(uri, "Remove @Stateless", d1, te1a);
        TextEdit te1b = te(8, 0, 10, 0, "@Dependent\n@Stateless\n");
        CodeAction ca1b = ca(uri, "Replace current scope with @Dependent", d1, te1b);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1a, ca1b);

        // Assert for diagnostic d2 - Two quickfixes: Remove @Stateless or Replace scope with @Dependent
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, d2);
        TextEdit te2a = te(14, 0, 15, 0, "");
        CodeAction ca2a = ca(uri, "Remove @Stateless", d2, te2a);
        TextEdit te2b = te(14, 0, 15, 14, "@Dependent\n@Stateless");
        CodeAction ca2b = ca(uri, "Replace current scope with @Dependent", d2, te2b);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca2a, ca2b);

        // Assert for diagnostic d3 - Two quickfixes: Remove @Stateless or Replace scopes with @Dependent
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, d3);
        TextEdit te3a = te(20, 0, 21, 0, "");
        CodeAction ca3a = ca(uri, "Remove @Stateless", d3, te3a);
        TextEdit te3b = te(20, 0, 22, 14, "@Dependent\n@Stateless");
        CodeAction ca3b = ca(uri, "Replace current scope with @Dependent", d3, te3b);
        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca3a, ca3b);
    }
}
