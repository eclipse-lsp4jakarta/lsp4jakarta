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

import com.google.gson.Gson;

public class SingletonSessionBeanTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void singletonSessionBeanWithInvalidScope() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SingletonSessionBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostics for singleton beans with invalid scopes

        // Test case 1: @Singleton with @RequestScoped (line 11)
        Diagnostic singletonWithRequestScoped = d(11, 13, 33,
                                                  "A singleton session bean belongs to either the @ApplicationScoped or @Dependent scope. any other scope is invalid.",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        singletonWithRequestScoped.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        // Test case 2: @Singleton with @SessionScoped (line 17)
        Diagnostic singletonWithSessionScoped = d(17, 6, 31,
                                                  "A singleton session bean belongs to either the @ApplicationScoped or @Dependent scope. any other scope is invalid.",
                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        singletonWithSessionScoped.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped")));

        // Test case 6: @Singleton with @RequestScoped + @ApplicationScoped (line 41)
        Diagnostic singletonWithRequestAndApplicationScoped = d(41, 6, 51,
                                                                "A singleton session bean belongs to either the @ApplicationScoped or @Dependent scope. any other scope is invalid.",
                                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        singletonWithRequestAndApplicationScoped.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped",
                                                                                             "jakarta.enterprise.context.RequestScoped")));

        // Test case 6 also triggers multiple scopes diagnostic
        Diagnostic multipleScopesForRequestAndApplicationScoped = d(41, 6, 51,
                                                                    "Scope type annotations must be specified by a managed bean class at most once.",
                                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        multipleScopesForRequestAndApplicationScoped.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped",
                                                                                                 "jakarta.enterprise.context.RequestScoped")));

        // Test case 7: @Singleton with @SessionScoped + @Dependent (line 48)
        Diagnostic singletonWithSessionAndDependent = d(48, 6, 43,
                                                        "A singleton session bean belongs to either the @ApplicationScoped or @Dependent scope. any other scope is invalid.",
                                                        DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        singletonWithSessionAndDependent.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent", "jakarta.enterprise.context.SessionScoped")));

        // Test case 7 also triggers multiple scopes diagnostic
        Diagnostic multipleScopesForSessionAndDependent = d(48, 6, 43,
                                                            "Scope type annotations must be specified by a managed bean class at most once.",
                                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        multipleScopesForSessionAndDependent.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent", "jakarta.enterprise.context.SessionScoped")));

        // Test case 8: @Singleton with @ApplicationScoped + @Dependent (line 55)
        // This triggers the "multiple scopes" diagnostic since CDI beans can have at most one scope
        Diagnostic singletonWithMultipleValidScopes = d(55, 6, 34,
                                                        "Scope type annotations must be specified by a managed bean class at most once.",
                                                        DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        singletonWithMultipleValidScopes.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent", "jakarta.enterprise.context.ApplicationScoped")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              singletonWithRequestScoped,
                              singletonWithSessionScoped,
                              singletonWithRequestAndApplicationScoped,
                              multipleScopesForRequestAndApplicationScoped,
                              singletonWithSessionAndDependent,
                              multipleScopesForSessionAndDependent,
                              singletonWithMultipleValidScopes);

        // Test code actions for singletonWithRequestScoped
        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, singletonWithRequestScoped);

        // Code action 1: Remove @Singleton annotation
        TextEdit removeSingleton1 = te(9, 0, 10, 0, "");
        CodeAction removeSingletonAction1 = ca(uri, "Remove @Singleton", singletonWithRequestScoped, removeSingleton1);

        // Code action 2: Replace @Singleton and @RequestScoped with @Dependent and @Singleton
        TextEdit replaceWithDependent1 = te(9, 0, 11, 0, "@Dependent\n@Singleton\n");
        CodeAction replaceWithDependentAction1 = ca(uri, "Replace current scope with @Dependent", singletonWithRequestScoped, replaceWithDependent1);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, removeSingletonAction1, replaceWithDependentAction1);

        // Test code actions for singletonWithSessionScoped
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, singletonWithSessionScoped);

        // Code action 1: Remove @Singleton annotation
        TextEdit removeSingleton2 = te(15, 0, 16, 0, "");
        CodeAction removeSingletonAction2 = ca(uri, "Remove @Singleton", singletonWithSessionScoped, removeSingleton2);

        // Code action 2: Replace @Singleton and @SessionScoped with @Dependent and @Singleton
        TextEdit replaceWithDependent2 = te(15, 0, 16, 14, "@Dependent\n@Singleton");
        CodeAction replaceWithDependentAction2 = ca(uri, "Replace current scope with @Dependent", singletonWithSessionScoped, replaceWithDependent2);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, removeSingletonAction2, replaceWithDependentAction2);

        // Test code actions for singletonWithRequestAndApplicationScoped
        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, singletonWithRequestAndApplicationScoped);

        // Code action 1: Remove @Singleton annotation
        TextEdit removeSingleton3 = te(38, 0, 39, 0, "");
        CodeAction removeSingletonAction3 = ca(uri, "Remove @Singleton", singletonWithRequestAndApplicationScoped, removeSingleton3);

        // Code action 2: Replace with @Dependent (removes @Singleton, @RequestScoped and @ApplicationScoped, then adds @Dependent and @Singleton)
        TextEdit replaceWithDependent3 = te(38, 0, 40, 18, "@Dependent\n@Singleton");
        CodeAction replaceWithDependentAction3 = ca(uri, "Replace current scope with @Dependent", singletonWithRequestAndApplicationScoped, replaceWithDependent3);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, removeSingletonAction3, replaceWithDependentAction3);

        // Test code actions for singletonWithSessionAndDependent
        JakartaJavaCodeActionParams codeActionParams4 = createCodeActionParams(uri, singletonWithSessionAndDependent);

        // Code action 1: Remove @Singleton annotation
        TextEdit removeSingleton4 = te(45, 0, 46, 0, "");
        CodeAction removeSingletonAction4 = ca(uri, "Remove @Singleton", singletonWithSessionAndDependent, removeSingleton4);

        // Code action 2: Replace with @Dependent (removes @Singleton, @SessionScoped and @Dependent, then adds @Dependent and @Singleton)
        TextEdit replaceWithDependent4 = te(45, 0, 47, 10, "@Dependent\n@Singleton");
        CodeAction replaceWithDependentAction4 = ca(uri, "Replace current scope with @Dependent", singletonWithSessionAndDependent, replaceWithDependent4);

        assertJavaCodeAction(codeActionParams4, IJDT_UTILS, removeSingletonAction4, replaceWithDependentAction4);
    }
}
