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

import com.google.gson.Gson;

/**
 * Tests that CDI scope inheritance is validated correctly for @Singleton and @Stateless
 * session beans. CDI scope annotations are @Inherited, so when a session bean declares
 * no scope of its own, the nearest ancestor's scope becomes its effective scope. If that
 * inherited scope is not valid for the bean type, a diagnostic must be raised.
 *
 * Test resources are in: io/openliberty/sample/jakarta/cdi/sessionbean/inheritance/
 */
public class SessionBeanScopeInheritanceTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -----------------------------------------------------------------------
    // @Singleton — inherits invalid @RequestScoped from direct superclass
    // File: SingletonInheritsRequestScope.java
    // @Singleton on line 8, class decl on line 9
    // "public class SingletonInheritsRequestScope" -> col 13..42
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonInheritsRequestScope() throws Exception {
        String uri = getFileUri("SingletonInheritsRequestScope.java");

        Diagnostic inheritedRequestScopeOnSingleton = d(8, 13, 42,
                                                        "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                                        DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        inheritedRequestScopeOnSingleton.setData(
                                                 new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, inheritedRequestScopeOnSingleton);
    }

    // -----------------------------------------------------------------------
    // @Singleton — inherits valid @ApplicationScoped: no diagnostic
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonInheritsApplicationScopeIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("SingletonInheritsApplicationScope.java")), IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // @Singleton — inherits valid @Dependent: no diagnostic
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonInheritsDependentScopeIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("SingletonInheritsDependentScope.java")), IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // @Singleton — inherits invalid @SessionScoped from direct superclass
    // File: SingletonInheritsSessionScope.java
    // @Singleton on line 8, class decl on line 9
    // "public class SingletonInheritsSessionScope" -> col 13..42
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonInheritsSessionScope() throws Exception {
        String uri = getFileUri("SingletonInheritsSessionScope.java");

        Diagnostic inheritedSessionScopeOnSingleton = d(8, 13, 42,
                                                        "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                                        DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        inheritedSessionScopeOnSingleton.setData(
                                                 new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, inheritedSessionScopeOnSingleton);
    }

    // -----------------------------------------------------------------------
    // @Singleton — inherits @RequestScoped transitively through intermediate class
    // File: SingletonInheritsRequestScopeTransitively.java
    // @Singleton on line 9, class decl on line 10
    // "public class SingletonInheritsRequestScopeTransitively" -> col 13..54
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonInheritsRequestScopeTransitively() throws Exception {
        String uri = getFileUri("SingletonInheritsRequestScopeTransitively.java");

        Diagnostic inheritedRequestScopeTransitiveOnSingleton = d(9, 13, 54,
                                                                  "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        inheritedRequestScopeTransitiveOnSingleton.setData(
                                                           new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, inheritedRequestScopeTransitiveOnSingleton);
    }

    // -----------------------------------------------------------------------
    // @Singleton — own @ApplicationScoped overrides inherited @RequestScoped: no diagnostic
    // -----------------------------------------------------------------------
    @Test
    public void testSingletonWithOwnScopeOverridesInheritedScopeIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("SingletonWithOwnScopeOverridesInheritedScope.java")), IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // @Stateless — inherits invalid @RequestScoped from direct superclass
    // File: StatelessInheritsRequestScope.java
    // @Stateless on line 8, class decl on line 9
    // "public class StatelessInheritsRequestScope" -> col 13..42
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessInheritsRequestScope() throws Exception {
        String uri = getFileUri("StatelessInheritsRequestScope.java");

        Diagnostic inheritedRequestScopeOnStateless = d(8, 13, 42,
                                                        "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                                        DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");
        inheritedRequestScopeOnStateless.setData(
                                                 new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, inheritedRequestScopeOnStateless);
    }

    // -----------------------------------------------------------------------
    // @Stateless — inherits valid @Dependent: no diagnostic
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessInheritsDependentScopeIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("StatelessInheritsDependentScope.java")), IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // @Stateless — inherits invalid @ApplicationScoped from direct superclass
    // File: StatelessInheritsApplicationScope.java
    // @Stateless on line 8, class decl on line 9
    // "public class StatelessInheritsApplicationScope" -> col 13..46
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessInheritsApplicationScope() throws Exception {
        String uri = getFileUri("StatelessInheritsApplicationScope.java");

        Diagnostic inheritedApplicationScopeOnStateless = d(8, 13, 46,
                                                            "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                                            DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");
        inheritedApplicationScopeOnStateless.setData(
                                                     new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, inheritedApplicationScopeOnStateless);
    }

    // -----------------------------------------------------------------------
    // @Stateless — inherits @RequestScoped transitively through intermediate class
    // File: StatelessInheritsRequestScopeTransitively.java
    // @Stateless on line 9, class decl on line 10
    // "public class StatelessInheritsRequestScopeTransitively" -> col 13..54
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessInheritsRequestScopeTransitively() throws Exception {
        String uri = getFileUri("StatelessInheritsRequestScopeTransitively.java");

        Diagnostic inheritedRequestScopeTransitiveOnStateless = d(9, 13, 54,
                                                                  "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                                                  DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");
        inheritedRequestScopeTransitiveOnStateless.setData(
                                                           new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, inheritedRequestScopeTransitiveOnStateless);
    }

    // -----------------------------------------------------------------------
    // @Stateless — own @Dependent overrides inherited @RequestScoped: no diagnostic
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessWithOwnScopeOverridesInheritedScopeIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("StatelessWithOwnScopeOverridesInheritedScope.java")), IJDT_UTILS);
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
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/sessionbean/inheritance/" + fileName));
        return javaFile.getLocation().toFile().toURI().toString();
    }
}
