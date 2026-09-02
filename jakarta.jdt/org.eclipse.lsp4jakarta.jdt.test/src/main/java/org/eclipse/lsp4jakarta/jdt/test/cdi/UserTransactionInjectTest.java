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
 * Tests for the CDI diagnostic that flags UserTransaction injection
 * via @Inject in a CDI-managed bean.
 *
 * According to CDI specification section 3.0, additional built-in beans:
 * https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#additional_builtin_beans
 * UserTransaction may only be injected in specific component contexts such as
 * servlets or application clients. Injecting it via @Inject in a CDI-managed
 * bean is a definition error.
 */
public class UserTransactionInjectTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    private static final String DIAGNOSTIC_MESSAGE = "The @Inject annotation must not be used to inject UserTransaction in a CDI-managed bean.";

    @Test
    public void userTransactionInjectedInCdiBean() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/UserTransactionInjectedInCdiBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic applicationScopedFieldDiagnostic = d(13, 28, 43,
                                                        DIAGNOSTIC_MESSAGE,
                                                        DiagnosticSeverity.Error, "jakarta-cdi", "InvalidUserTransactionInjectionInCDIBean");

        Diagnostic requestScopedFieldDiagnostic = d(25, 28, 30,
                                                    DIAGNOSTIC_MESSAGE,
                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidUserTransactionInjectionInCDIBean");

        Diagnostic sessionScopedFieldDiagnostic = d(37, 28, 37,
                                                    DIAGNOSTIC_MESSAGE,
                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidUserTransactionInjectionInCDIBean");

        Diagnostic initMethodParamDiagnostic = d(49, 16, 20,
                                                 DIAGNOSTIC_MESSAGE,
                                                 DiagnosticSeverity.Error, "jakarta-cdi", "InvalidUserTransactionInjectionInCDIBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              applicationScopedFieldDiagnostic,
                              requestScopedFieldDiagnostic,
                              sessionScopedFieldDiagnostic,
                              initMethodParamDiagnostic);

        JakartaJavaCodeActionParams applicationScopedFieldCodeActionParams = createCodeActionParams(uri, applicationScopedFieldDiagnostic);
        TextEdit removeInjectFromApplicationScopedField = te(12, 4, 13, 4, "");
        CodeAction removeInjectForApplicationScopedField = ca(uri, "Remove @Inject", applicationScopedFieldDiagnostic, removeInjectFromApplicationScopedField);
        assertJavaCodeAction(applicationScopedFieldCodeActionParams, IJDT_UTILS, removeInjectForApplicationScopedField);

        JakartaJavaCodeActionParams requestScopedFieldCodeActionParams = createCodeActionParams(uri, requestScopedFieldDiagnostic);
        TextEdit removeInjectFromRequestScopedField = te(24, 4, 25, 4, "");
        CodeAction removeInjectForRequestScopedField = ca(uri, "Remove @Inject", requestScopedFieldDiagnostic, removeInjectFromRequestScopedField);
        assertJavaCodeAction(requestScopedFieldCodeActionParams, IJDT_UTILS, removeInjectForRequestScopedField);

        JakartaJavaCodeActionParams sessionScopedFieldCodeActionParams = createCodeActionParams(uri, sessionScopedFieldDiagnostic);
        TextEdit removeInjectFromSessionScopedField = te(36, 4, 37, 4, "");
        CodeAction removeInjectForSessionScopedField = ca(uri, "Remove @Inject", sessionScopedFieldDiagnostic, removeInjectFromSessionScopedField);
        assertJavaCodeAction(sessionScopedFieldCodeActionParams, IJDT_UTILS, removeInjectForSessionScopedField);

        JakartaJavaCodeActionParams initMethodCodeActionParams = createCodeActionParams(uri, initMethodParamDiagnostic);
        TextEdit removeInjectFromInitMethod = te(48, 4, 49, 4, "");
        CodeAction removeInjectForInitMethod = ca(uri, "Remove @Inject", initMethodParamDiagnostic, removeInjectFromInitMethod);
        assertJavaCodeAction(initMethodCodeActionParams, IJDT_UTILS, removeInjectForInitMethod);
    }

    @Test
    public void validUserTransactionUsage() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/ValidUserTransactionUsage.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
