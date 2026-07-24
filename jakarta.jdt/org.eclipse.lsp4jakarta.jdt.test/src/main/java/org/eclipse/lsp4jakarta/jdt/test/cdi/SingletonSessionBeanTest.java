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

/**
 * Tests for CDI scope validation on @Singleton session beans.
 * Test resources are under: io/openliberty/sample/jakarta/cdi/sessionbean/
 *
 * All files share this structure (0-based lines):
 * line 6: @Singleton
 * line 7: @XxxScoped (first scope, if any)
 * line 8: @YyyScoped (second scope, if 3-annotation files)
 * line 8 or 9: public class ClassName {
 *
 * Class name always starts at col 13 ("public class " = 13 chars).
 */
public class SingletonSessionBeanTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -----------------------------------------------------------------------
    // SingletonSessionBean.java — @Singleton + @RequestScoped (invalid)
    // 3 imports → @Singleton on line 7, @RequestScoped on line 8, class decl line 9
    // "public class SingletonSessionBean {" → col 13..33
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithRequestScope() throws Exception {
        String uri = getFileUri("SingletonSessionBean.java");

        Diagnostic invalidScope = d(9, 13, 33,
                                    "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        invalidScope.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, invalidScope);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, invalidScope);
        TextEdit removeSingleton = te(7, 0, 8, 0, "");
        TextEdit replaceWithDependent = te(7, 0, 9, 0, "@Dependent\n@Singleton\n");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Singleton", invalidScope, removeSingleton),
                             ca(uri, "Replace current scope with @Dependent", invalidScope, replaceWithDependent));
    }

    // -----------------------------------------------------------------------
    // SingletonWithSessionScope.java — @Singleton + @SessionScoped (invalid)
    // 3 imports → @Singleton on line 7, @SessionScoped on line 8, class decl line 9
    // "public class SingletonWithSessionScope {" → col 13..38
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithSessionScope() throws Exception {
        String uri = getFileUri("SingletonWithSessionScope.java");

        Diagnostic invalidScope = d(9, 13, 38,
                                    "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        invalidScope.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, invalidScope);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, invalidScope);
        TextEdit removeSingleton = te(7, 0, 8, 0, "");
        TextEdit replaceWithDependent = te(7, 0, 9, 0, "@Dependent\n@Singleton\n");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Singleton", invalidScope, removeSingleton),
                             ca(uri, "Replace current scope with @Dependent", invalidScope, replaceWithDependent));
    }

    // -----------------------------------------------------------------------
    // SingletonWithApplicationScope.java — @Singleton + @ApplicationScoped (valid)
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithApplicationScopeIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("SingletonWithApplicationScope.java")), IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // SingletonWithDependent.java — @Singleton + @Dependent (valid)
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithDependentIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("SingletonWithDependent.java")), IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // SingletonWithNoScope.java — @Singleton only (valid — default scope)
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithNoScopeIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("SingletonWithNoScope.java")), IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // SingletonWithMixedInvalidAndApplicationScoped.java
    //   4 imports → @Singleton line 8, @RequestScoped line 9, @ApplicationScoped line 10, class decl line 11
    //   "public class SingletonWithMixedInvalidAndApplicationScoped {" → col 13..58
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithMixedInvalidAndApplicationScoped() throws Exception {
        String uri = getFileUri("SingletonWithMixedInvalidAndApplicationScoped.java");

        Diagnostic invalidScope = d(11, 13, 58,
                                    "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        invalidScope.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped",
                                                                 "jakarta.enterprise.context.RequestScoped")));

        Diagnostic multipleScopes = d(11, 13, 58,
                                      "Scope type annotations must be specified by a managed bean class at most once.",
                                      DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        multipleScopes.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped",
                                                                   "jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, invalidScope, multipleScopes);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, invalidScope);
        TextEdit removeSingleton = te(8, 0, 9, 0, "");
        TextEdit replaceWithDependent = te(8, 0, 11, 0, "@Dependent\n@Singleton\n");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Singleton", invalidScope, removeSingleton),
                             ca(uri, "Replace current scope with @Dependent", invalidScope, replaceWithDependent));
    }

    // -----------------------------------------------------------------------
    // SingletonWithMixedInvalidAndDependent.java
    //   @Singleton + @SessionScoped + @Dependent (invalid + multiple scopes)
    //   class decl line 10, "public class SingletonWithMixedInvalidAndDependent {" → col 13..50
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithMixedInvalidAndDependent() throws Exception {
        String uri = getFileUri("SingletonWithMixedInvalidAndDependent.java");

        Diagnostic invalidScope = d(10, 13, 50,
                                    "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        invalidScope.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent",
                                                                 "jakarta.enterprise.context.SessionScoped")));

        Diagnostic multipleScopes = d(10, 13, 50,
                                      "Scope type annotations must be specified by a managed bean class at most once.",
                                      DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        multipleScopes.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent",
                                                                   "jakarta.enterprise.context.SessionScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, invalidScope, multipleScopes);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, invalidScope);
        TextEdit removeSingleton = te(7, 0, 8, 0, "");
        TextEdit replaceWithDependent = te(7, 0, 10, 0, "@Dependent\n@Singleton\n");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Singleton", invalidScope, removeSingleton),
                             ca(uri, "Replace current scope with @Dependent", invalidScope, replaceWithDependent));
    }

    // -----------------------------------------------------------------------
    // SingletonWithBothValidScopes.java
    //   @Singleton + @ApplicationScoped + @Dependent (multiple scopes — invalid)
    //   class decl line 10, "public class SingletonWithBothValidScopes {" → col 13..41
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithBothValidScopes() throws Exception {
        String uri = getFileUri("SingletonWithBothValidScopes.java");

        Diagnostic multipleScopes = d(10, 13, 41,
                                      "Scope type annotations must be specified by a managed bean class at most once.",
                                      DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        multipleScopes.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent",
                                                                   "jakarta.enterprise.context.ApplicationScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, multipleScopes);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private JakartaJavaDiagnosticsParams createDiagnosticsParams(String uri) {
        JakartaJavaDiagnosticsParams p = new JakartaJavaDiagnosticsParams();
        p.setUris(Arrays.asList(uri));
        return p;
    }

    private String getFileUri(String fileName) throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/sessionbean/" + fileName));
        return javaFile.getLocation().toFile().toURI().toString();
    }
}
