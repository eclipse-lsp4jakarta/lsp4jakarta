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

/**
 * Tests for Jakarta Security Identity Store Definition diagnostics and quickfixes.
 *
 * Validates that beans annotated with @LdapIdentityStoreDefinition or
 *
 * @DatabaseIdentityStoreDefinition comply with the Jakarta Security specification
 *                                  by having the required @ApplicationScoped scope annotation, and that the
 *                                  appropriate quickfixes are offered to resolve violations.
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

        // Test quickfix: insert @ApplicationScoped (with import) before the class declaration.
        // InsertAnnotationProposal produces a single edit spanning from the import section
        // to just before the class declaration, replacing/adding the import and the annotation.
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, missingApplicationScoped);
        TextEdit insertAnnotation = te(2, 0, 4, 0,
                                       "import jakarta.enterprise.context.ApplicationScoped;\n"
                                                   + "import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;\n"
                                                   + "\n"
                                                   + "@ApplicationScoped\n");
        CodeAction insertAction = ca(uri, "Insert @ApplicationScoped", missingApplicationScoped, insertAnnotation);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertAction);
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
        wrongScope.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, wrongScope);

        // Test quickfix: replace @RequestScoped with @ApplicationScoped.
        // ReplaceAnnotationProposal produces a single edit spanning from the import section
        // to just before the class declaration, rewriting imports and removing the old annotation.
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, wrongScope);
        TextEdit replaceAnnotation = te(2, 0, 11, 0,
                                        "import jakarta.enterprise.context.ApplicationScoped;\n"
                                                     + "import jakarta.enterprise.context.RequestScoped;\n"
                                                     + "import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;\n"
                                                     + "\n"
                                                     + "@ApplicationScoped\n"
                                                     + "@LdapIdentityStoreDefinition(\n"
                                                     + "    url = \"ldap://localhost:10389\",\n"
                                                     + "    callerBaseDn = \"ou=caller,dc=jsr375,dc=net\",\n"
                                                     + "    groupSearchBase = \"ou=group,dc=jsr375,dc=net\"\n"
                                                     + ")\n");
        CodeAction replaceAction = ca(uri, "Replace @RequestScoped with @ApplicationScoped", wrongScope, replaceAnnotation);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, replaceAction);
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

        // Test quickfix: insert @ApplicationScoped (with import) before the class declaration.
        // InsertAnnotationProposal produces a single edit spanning from the import section
        // to just before the class declaration, replacing/adding the import and the annotation.
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, missingApplicationScoped);
        TextEdit insertAnnotation = te(2, 0, 4, 0,
                                       "import jakarta.enterprise.context.ApplicationScoped;\n"
                                                   + "import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;\n"
                                                   + "\n"
                                                   + "@ApplicationScoped\n");
        CodeAction insertAction = ca(uri, "Insert @ApplicationScoped", missingApplicationScoped, insertAnnotation);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, insertAction);
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
        wrongScope.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, wrongScope);

        // Test quickfix: replace @RequestScoped with @ApplicationScoped.
        // ReplaceAnnotationProposal produces a single edit spanning from the import section
        // to just before the class declaration, rewriting imports and removing the old annotation.
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, wrongScope);
        TextEdit replaceAnnotation = te(2, 0, 11, 0,
                                        "import jakarta.enterprise.context.ApplicationScoped;\n"
                                                     + "import jakarta.enterprise.context.RequestScoped;\n"
                                                     + "import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;\n"
                                                     + "\n"
                                                     + "@ApplicationScoped\n"
                                                     + "@DatabaseIdentityStoreDefinition(\n"
                                                     + "    dataSourceLookup = \"java:comp/DefaultDataSource\",\n"
                                                     + "    callerQuery = \"select password from caller where name = ?\",\n"
                                                     + "    groupsQuery = \"select group_name from caller_groups where caller_name = ?\"\n"
                                                     + ")\n");
        CodeAction replaceAction = ca(uri, "Replace @RequestScoped with @ApplicationScoped", wrongScope, replaceAnnotation);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, replaceAction);
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
        wrongScope.setData(new Gson().toJsonTree(Arrays.asList("jakarta.interceptor.Interceptor")));

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

        // CDI diagnostic: Decorator must have @Delegate injection point
        Diagnostic cdiDiagnostic = d(11, 13, 52,
                                     "A decorator must declare exactly one injection point annotated with @Delegate.",
                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidDecoratorDelegateInjectionPoints");

        // Security diagnostic: @Decorator is treated as a scope annotation
        Diagnostic wrongScope = d(11, 13, 52,
                                  "A class annotated with @DatabaseIdentityStoreDefinition must be annotated with @ApplicationScoped, instead of @Decorator.",
                                  DiagnosticSeverity.Error, "jakarta-security", "InvalidScopeOnIdentityStoreDefinition");
        wrongScope.setData(new Gson().toJsonTree(Arrays.asList("jakarta.decorator.Decorator")));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, cdiDiagnostic, wrongScope);
    }
}
