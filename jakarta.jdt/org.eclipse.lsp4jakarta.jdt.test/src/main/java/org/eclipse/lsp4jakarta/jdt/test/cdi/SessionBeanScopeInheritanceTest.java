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
 */
public class SessionBeanScopeInheritanceTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void sessionBeanInheritedScopeValidation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/SingletonSessionBeanInheritance.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // --- @Singleton inheritance cases ---

        // Test case 1: @Singleton inherits @RequestScoped from direct superclass — invalid
        // Class name "SingletonInheritsRequestScope" (29 chars) on line 47 (0-based: 46), col 6-35
        Diagnostic singletonInheritsRequestScope = d(46, 6, 35,
                                                     "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        singletonInheritsRequestScope.setData(
                                              new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        // Test case 4: @Singleton inherits @SessionScoped from direct superclass — invalid
        // Class name "SingletonInheritsSessionScope" (29 chars) on line 62 (0-based: 61), col 6-35
        Diagnostic singletonInheritsSessionScope = d(61, 6, 35,
                                                     "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        singletonInheritsSessionScope.setData(
                                              new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.SessionScoped")));

        // Test case 5: @Singleton inherits @RequestScoped transitively (grandparent -> intermediate -> child)
        // Class name "SingletonInheritsRequestScopeTransitively" (41 chars) on line 68 (0-based: 67), col 6-47
        Diagnostic singletonInheritsRequestScopeTransitively = d(67, 6, 47,
                                                                 "A singleton session bean must be annotated with either @ApplicationScoped or @Dependent.",
                                                                 DiagnosticSeverity.Error, "jakarta-cdi", "InvalidSingletonSessionBeanScope");
        singletonInheritsRequestScopeTransitively.setData(
                                                          new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        // --- @Stateless inheritance cases ---

        // Test case 7: @Stateless inherits @RequestScoped from direct superclass — invalid
        // Class name "StatelessInheritsRequestScope" (29 chars) on line 84 (0-based: 83), col 6-35
        Diagnostic statelessInheritsRequestScope = d(83, 6, 35,
                                                     "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");
        statelessInheritsRequestScope.setData(
                                              new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        // Test case 9: @Stateless inherits @ApplicationScoped from direct superclass — invalid
        // Class name "StatelessInheritsApplicationScope" (33 chars) on line 94 (0-based: 93), col 6-39
        Diagnostic statelessInheritsApplicationScope = d(93, 6, 39,
                                                         "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                                         DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");
        statelessInheritsApplicationScope.setData(
                                                  new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.ApplicationScoped")));

        // Test case 10: @Stateless inherits @RequestScoped transitively — invalid
        // Class name "StatelessInheritsRequestScopeTransitively" (41 chars) on line 99 (0-based: 98), col 6-47
        Diagnostic statelessInheritsRequestScopeTransitively = d(98, 6, 47,
                                                                 "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                                                 DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");
        statelessInheritsRequestScopeTransitively.setData(
                                                          new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.RequestScoped")));

        // Valid cases (test cases 2, 3, 6, 8, 11) must produce no diagnostics:
        //   - @Singleton inheriting @ApplicationScoped (valid)
        //   - @Singleton inheriting @Dependent (valid)
        //   - @Singleton with own @ApplicationScoped overriding inherited @RequestScoped (valid)
        //   - @Stateless inheriting @Dependent (valid)
        //   - @Stateless with own @Dependent overriding inherited @RequestScoped (valid)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              singletonInheritsRequestScope,
                              singletonInheritsSessionScope,
                              singletonInheritsRequestScopeTransitively,
                              statelessInheritsRequestScope,
                              statelessInheritsApplicationScope,
                              statelessInheritsRequestScopeTransitively);
    }
}
