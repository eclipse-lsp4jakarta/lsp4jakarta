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
package org.eclipse.lsp4jakarta.jdt.test.security;

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
 * Tests for Jakarta Security Identity Store Definition diagnostics.
 *
 * Validates that beans annotated with @LdapIdentityStoreDefinition or
 *
 * @DatabaseIdentityStoreDefinition comply with the Jakarta Security specification
 *                                  by having the required @ApplicationScoped scope annotation.
 */
public class SecurityIdentityStoreTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void ldapIdentityStoreValidTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/security/identitystore/LdapIdentityStoreValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected for valid identity store
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void ldapIdentityStoreMissingApplicationScopedTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/security/identitystore/LdapIdentityStoreMissingApplicationScoped.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for missing @ApplicationScoped
        Diagnostic missingApplicationScoped = d(9, 13, 54,
                                                "A class annotated with @LdapIdentityStoreDefinition must be annotated with @ApplicationScoped.",
                                                DiagnosticSeverity.Error, "jakarta-security", "MissingApplicationScopedOnIdentityStoreDefinition");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, missingApplicationScoped);
    }

    @Test
    public void ldapIdentityStoreWithWrongScopeTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/security/identitystore/LdapIdentityStoreWithWrongScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for wrong scope annotation
        Diagnostic wrongScope = d(11, 13, 44,
                                  "A class annotated with @LdapIdentityStoreDefinition must be annotated with @ApplicationScoped, instead of @RequestScoped.",
                                  DiagnosticSeverity.Error, "jakarta-security", "InvalidScopeOnIdentityStoreDefinition");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, wrongScope);
    }

    @Test
    public void databaseIdentityStoreValidTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/security/identitystore/DatabaseIdentityStoreValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected for valid identity store
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void databaseIdentityStoreMissingApplicationScopedTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/security/identitystore/DatabaseIdentityStoreMissingApplicationScoped.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for missing @ApplicationScoped
        Diagnostic missingApplicationScoped = d(9, 13, 58,
                                                "A class annotated with @DatabaseIdentityStoreDefinition must be annotated with @ApplicationScoped.",
                                                DiagnosticSeverity.Error, "jakarta-security", "MissingApplicationScopedOnIdentityStoreDefinition");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, missingApplicationScoped);
    }

    @Test
    public void databaseIdentityStoreWithWrongScopeTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/security/identitystore/DatabaseIdentityStoreWithWrongScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for wrong scope annotation
        Diagnostic wrongScope = d(11, 13, 48,
                                  "A class annotated with @DatabaseIdentityStoreDefinition must be annotated with @ApplicationScoped, instead of @RequestScoped.",
                                  DiagnosticSeverity.Error, "jakarta-security", "InvalidScopeOnIdentityStoreDefinition");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, wrongScope);
    }

    @Test
    public void ldapIdentityStoreWithInterceptorScopeTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/security/identitystore/LdapIdentityStoreWithInterceptorScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for @Interceptor scope annotation
        Diagnostic wrongScope = d(11, 13, 50,
                                  "A class annotated with @LdapIdentityStoreDefinition must be annotated with @ApplicationScoped, instead of @Interceptor.",
                                  DiagnosticSeverity.Error, "jakarta-security", "InvalidScopeOnIdentityStoreDefinition");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, wrongScope);
    }

    @Test
    public void databaseIdentityStoreWithDecoratorScopeTest() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");

        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/security/identitystore/DatabaseIdentityStoreWithDecoratorScope.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for @Decorator scope annotation
        Diagnostic wrongScope = d(11, 13, 52,
                                  "A class annotated with @DatabaseIdentityStoreDefinition must be annotated with @ApplicationScoped, instead of @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-security", "InvalidScopeOnIdentityStoreDefinition");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, wrongScope);
    }
}
