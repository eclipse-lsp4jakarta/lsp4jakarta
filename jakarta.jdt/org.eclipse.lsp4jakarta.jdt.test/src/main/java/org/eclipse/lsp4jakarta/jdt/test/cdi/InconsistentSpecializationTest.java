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

/**
 * Tests for CDI inconsistent specialization diagnostic (CDI 3.0 §5.1.3).
 * Only one bean may specialize a given base bean.
 */
public class InconsistentSpecializationTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // Message template helper — mirrors InconsistentSpecialization in messages.properties
    private static String msg(String beanName, String baseTypeFqName) {
        return "Inconsistent specialization: bean '" + beanName
               + "' and at least one other bean both specialize the same base type '"
               + baseTypeFqName + "'. Only one bean may specialize a given base bean.";
    }

    /** BaseBean.java — no @Specializes, no diagnostic expected. */
    @Test
    public void noDiagnosticOnBaseBeanWithoutSpecializes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/specializes/BaseBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /** ValidSpecializer.java — sole specializer of ValidBaseBean, no diagnostic expected. */
    @Test
    public void noDiagnosticWhenOnlyOneBeanSpecializesABase() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/specializes/ValidSpecializer.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * SpecializerA.java — SpecializerB also takes over BaseBean, conflict.
     * Diagnostic expected on the class name (line 14, col 13–25).
     */
    @Test
    public void inconsistentSpecializationOnFirstBean() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/specializes/SpecializerA.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(8, 13, 25,
                         msg("SpecializerA",
                             "io.openliberty.sample.jakarta.cdi.specializes.BaseBean"),
                         DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInconsistentSpecialization");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

    /**
     * SpecializerB.java — SpecializerA also takes over BaseBean, conflict.
     * Diagnostic expected on the class name (line 8, col 13–25).
     */
    @Test
    public void inconsistentSpecializationOnSecondBean() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/specializes/SpecializerB.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(8, 13, 25,
                         msg("SpecializerB",
                             "io.openliberty.sample.jakarta.cdi.specializes.BaseBean"),
                         DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInconsistentSpecialization");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

    /**
     * TransitiveSpecializerB.java — transitively takes over TransitiveBaseBean via
     * TransitiveSpecializerA, which also directly takes over it — conflict.
     * Diagnostic expected on the class name (line 16, col 13–35).
     */
    @Test
    public void inconsistentSpecializationOnTransitiveBean() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/specializes/TransitiveSpecializerB.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(8, 13, 35,
                         msg("TransitiveSpecializerB",
                             "io.openliberty.sample.jakarta.cdi.specializes.TransitiveBaseBean"),
                         DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInconsistentSpecialization");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

}
