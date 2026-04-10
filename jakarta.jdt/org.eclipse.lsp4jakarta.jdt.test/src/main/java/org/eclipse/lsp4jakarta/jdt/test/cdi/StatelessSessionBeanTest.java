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
        Diagnostic statelessWithRequestScoped = d(10, 13, 33,
                                                  "A stateless session bean belongs to the @Dependent scope. any other scope is invalid.",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");

        Diagnostic statelessWithSessionScoped = d(16, 6, 32,
                                                  "A stateless session bean belongs to the @Dependent scope. any other scope is invalid.",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");

        Diagnostic statelessWithApplicationScoped = d(23, 6, 33,
                                                      "A stateless session bean belongs to the @Dependent scope. any other scope is invalid.",
                                                      DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, statelessWithRequestScoped, statelessWithSessionScoped, statelessWithApplicationScoped);

        // Assert for diagnostic statelessWithRequestScoped - Two quickfixes: Remove @Stateless or Replace scope with @Dependent
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, statelessWithRequestScoped);
        TextEdit removeStateless1 = te(8, 0, 9, 0, "");
        CodeAction removeStatelessAction1 = ca(uri, "Remove @Stateless", statelessWithRequestScoped, removeStateless1);
        TextEdit replaceWithDependent1 = te(8, 0, 10, 0, "@Dependent\n@Stateless\n");
        CodeAction replaceWithDependentAction1 = ca(uri, "Replace current scope with @Dependent", statelessWithRequestScoped, replaceWithDependent1);
        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, removeStatelessAction1, replaceWithDependentAction1);

        // Assert for diagnostic statelessWithSessionScoped - Two quickfixes: Remove @Stateless or Replace scope with @Dependent
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, statelessWithSessionScoped);
        TextEdit removeStateless2 = te(14, 0, 15, 0, "");
        CodeAction removeStatelessAction2 = ca(uri, "Remove @Stateless", statelessWithSessionScoped, removeStateless2);
        TextEdit replaceWithDependent2 = te(14, 0, 15, 14, "@Dependent\n@Stateless");
        CodeAction replaceWithDependentAction2 = ca(uri, "Replace current scope with @Dependent", statelessWithSessionScoped, replaceWithDependent2);
        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, removeStatelessAction2, replaceWithDependentAction2);

        // Assert for diagnostic statelessWithApplicationScoped - Two quickfixes: Remove @Stateless or Replace scopes with @Dependent
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, statelessWithApplicationScoped);
        TextEdit removeStateless3 = te(20, 0, 21, 0, "");
        CodeAction removeStatelessAction3 = ca(uri, "Remove @Stateless", statelessWithApplicationScoped, removeStateless3);
        TextEdit replaceWithDependent3 = te(20, 0, 22, 14, "@Dependent\n@Stateless");
        CodeAction replaceWithDependentAction3 = ca(uri, "Replace current scope with @Dependent", statelessWithApplicationScoped, replaceWithDependent3);
        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, removeStatelessAction3, replaceWithDependentAction3);
    }
}
