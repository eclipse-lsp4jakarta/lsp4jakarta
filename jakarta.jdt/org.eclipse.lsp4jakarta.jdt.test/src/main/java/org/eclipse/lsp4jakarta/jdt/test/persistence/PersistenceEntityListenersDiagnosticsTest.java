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

package org.eclipse.lsp4jakarta.jdt.test.persistence;

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
 * Tests for {@code @EntityListeners} diagnostics.
 */
public class PersistenceEntityListenersDiagnosticsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void testEntityListenersInvalidConstructor() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/entitylisteners/EntityListenersInvalidConstructor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic expectedDiagnostic = d(7, 0, 96,
                                          "The entity listener class(es) ProtectedConstructorListener, ParameterizedConstructorListener must declare a public no-argument constructor.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidConstructorInEntityListener");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);
    }

    @Test
    public void testEntityListenersValidConstructor() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/entitylisteners/EntityListenersValidConstructor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testEntityListenersInnerClassConstructor() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/entitylisteners/EntityListenersInnerClassConstructor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic expectedDiagnostic = d(7, 0, 142,
                                          "The entity listener class(es) NonStaticInnerImplicitListener, NonStaticInnerExplicitListener must not be abstract, an interface, a non-static inner class, an anonymous class, or a local class.",
                                          DiagnosticSeverity.Error, "jakarta-persistence", "InvalidEntityListenerType");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, expectedDiagnostic);
    }

    @Test
    public void testEntityListenersStaticNestedConstructor() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/entitylisteners/EntityListenersStaticNestedConstructor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void testEntityListenersAbstractAndPackagePrivateConstructor() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/entitylisteners/EntityListenersAbstractAndPackagePrivateConstructor.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic nonInstantiableDiagnostic = d(7, 0, 85,
                                                 "The entity listener class(es) AbstractListener must not be abstract, an interface, a non-static inner class, an anonymous class, or a local class.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "InvalidEntityListenerType");
        Diagnostic invalidConstructorDiagnostic = d(7, 0, 85,
                                                    "The entity listener class(es) PackagePrivateConstructorListener must declare a public no-argument constructor.",
                                                    DiagnosticSeverity.Error, "jakarta-persistence", "InvalidConstructorInEntityListener");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, nonInstantiableDiagnostic, invalidConstructorDiagnostic);
    }
}
