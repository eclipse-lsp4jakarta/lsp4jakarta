/*******************************************************************************
* Copyright (c) 2024, 2025 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.test.di;

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
 * Tests injection point object diagnostics.
 */
public class InjectionPointBeanValidationTests extends BaseJakartaTest {

    /**
     * JDT Utility class.
     */
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that is an abstract object.
     *
     * @throws Exception
     */
    @Test
    public void abstractClassConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/AbstractInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 52, 54,
                          "The parameter should not contain the abstract modifier. If it contains the abstract modifier, the class should be annotated with @Decorator.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidAbstractClassBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);
    }

    /**
     * Tests that no diagnostics are issued for an abstract class annotated with @Decorator.
     *
     * @throws Exception
     */
    @Test
    public void abstractClassDecoratorConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/AbstractDecoratorInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, new Diagnostic[] {});
    }

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that is an inner class.
     *
     * @throws Exception
     */
    @Test
    public void innerClassConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/InnerClassInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 48, 55,
                          "The parameter should not be an inner class.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidInnerClassBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);
    }

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that is an extension service.
     *
     * @throws Exception
     */
    @Test
    public void extensionServiceConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/ExtensionInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 61, 64,
                          "The parameter should not implement the jakarta.enterprise.inject.spi.Extension interface either directly or through a superclass or through super interface.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidExtensionProviderBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);
    }

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that is an extension service
     * through a super class.
     *
     * @throws Exception
     */
    @Test
    public void superClassExtensionServiceConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/SuperClassExtensionInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 98, 105,
                          "The parameter should not implement the jakarta.enterprise.inject.spi.Extension interface either directly or through a superclass or through super interface.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidExtensionProviderBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);
    }

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that is an extension service
     * through a super interface.
     *
     * @throws Exception
     */
    @Test
    public void superInterfaceExtensionServiceConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/SuperInterfaceExtensionInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 97, 103,
                          "The parameter should not implement the jakarta.enterprise.inject.spi.Extension interface either directly or through a superclass or through super interface.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidExtensionProviderBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);
    }

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that is a primitive.
     *
     * @throws Exception
     */
    @Test
    public void primitiveConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/PrimitiveParamConstructorInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(21, 61, 62,
                          "The parameter should not be a primitive type.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidPrimitiveBean");
        Diagnostic d2 = d(21, 68, 69,
                          "The parameter should not be a primitive type.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidPrimitiveBean");
        Diagnostic d3 = d(21, 76, 78,
                          "The parameter should not be a primitive type.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidPrimitiveBean");
        Diagnostic d4 = d(21, 86, 87,
                          "The parameter should not be a primitive type.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidPrimitiveBean");
        Diagnostic d5 = d(21, 96, 98,
                          "The parameter should not be a primitive type.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidPrimitiveBean");
        Diagnostic d6 = d(21, 105, 106,
                          "The parameter should not be a primitive type.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidPrimitiveBean");
        Diagnostic d7 = d(21, 113, 115,
                          "The parameter should not be a primitive type.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidPrimitiveBean");
        Diagnostic d8 = d(21, 125, 126,
                          "The parameter should not be a primitive type.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidPrimitiveBean");
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3, d4, d5, d6, d7, d8);
    }

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that is a vetoed bean.
     *
     * @throws Exception
     */
    @Test
    public void vetoedConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/VetoedInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 48, 50,
                          "The parameter should not be annotated with @Vetoed either directly or through the package-info metadata.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidVetoedClassBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);
    }

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that is a vetoed bean.
     *
     * @throws Exception
     */
    @Test
    public void vetoedPackageConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/VetoedPackageInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 62, 65,
                          "The parameter should not be annotated with @Vetoed either directly or through the package-info metadata.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidVetoedClassBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);
    }

    /**
     * Tests diagnostic issued for a bean constructor parameter injection point that does not define
     * a class with a valid constructor. A valid constructor is one that has no parameters or one that
     * is annotated with @Inject.
     *
     * @throws Exception
     */
    @Test
    public void invalidConstructorParam() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/di/InvalidConstructorInjectionPointUser.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test expected diagnostic
        Diagnostic d1 = d(22, 72, 75,
                          "The parameter should define a constructor with no parameters or a constructor annotated with @Inject.",
                          DiagnosticSeverity.Warning, "jakarta-di", "InjectionPointInvalidConstructorBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1);
    }
}
