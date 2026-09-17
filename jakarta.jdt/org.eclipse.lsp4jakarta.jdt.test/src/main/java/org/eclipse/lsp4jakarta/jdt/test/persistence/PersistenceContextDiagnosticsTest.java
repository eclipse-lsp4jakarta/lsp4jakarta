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
 * Integration tests for {@code @PersistenceContext} injection rules:
 * <ul>
 * <li>§7.6 — {@code @PersistenceContext} must only appear in a container-managed component
 * (CDI bean, EJB session bean, or Servlet component).</li>
 * <li>§7.6.3 — {@code PersistenceContextType.EXTENDED} can only be used in a {@code @Stateful}
 * EJB session bean.</li>
 * </ul>
 */
public class PersistenceContextDiagnosticsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -------------------------------------------------------------------------
    // §7.6 — @PersistenceContext must only appear in a managed component
    // -------------------------------------------------------------------------

    /**
     * Tests that @PersistenceContext on a field in a plain (unmanaged) Java class
     * produces a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     * §7.6 Container-managed Persistence Contexts: the entity manager lifecycle is managed by the
     * container and its injection is only valid in container-managed components (EJB, CDI, Servlet).
     */
    @Test
    public void persistenceContextInPlainClass() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInPlainClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Field "em" at 0-based line 11, columns 26-28.
        Diagnostic notInManagedComponentDiag = d(11, 26, 28,
                                                 "@PersistenceContext can only be used in a container-managed component such as a CDI bean, EJB, or Servlet.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "PersistenceContextNotInManagedComponent");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, notInManagedComponentDiag);
    }

    /**
     * Tests that @PersistenceContext in a CDI managed bean (@ApplicationScoped)
     * does NOT produce a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextInCdiBeanValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInCdiBeanValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — class is annotated with @ApplicationScoped (CDI managed bean)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that @PersistenceContext in a CDI bean with @Dependent scope
     * does NOT produce a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextInDependentBeanValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInDependentBeanValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — class is annotated with @Dependent (CDI managed bean)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that @PersistenceContext in an EJB (@Stateless session bean)
     * does NOT produce a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextInEjbValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInEjbValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — class is annotated with @Stateless (EJB session bean)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that @PersistenceContext in a Jakarta Servlet (@WebServlet + HttpServlet)
     * does NOT produce a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextInServletValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInServletValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — class is annotated with @WebServlet and extends HttpServlet
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that @PersistenceContext in a @WebFilter component does NOT produce a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextInWebFilterValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInWebFilterValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — class is annotated with @WebFilter (servlet component)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that @PersistenceContext in a @WebListener component does NOT produce a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextInWebListenerValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInWebListenerValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — class is annotated with @WebListener (servlet component)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * @PersistenceContext in an @Entity class — invalid; @Entity is NOT a managed injection
     *                     component. A diagnostic must be produced.
     *                     Spec §7.6: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextInEntityClassInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInEntityClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // field "em" at 0-based line 17, col 26-28
        Diagnostic notInManagedComponentDiag = d(17, 26, 28,
                                                 "@PersistenceContext can only be used in a container-managed component such as a CDI bean, EJB, or Servlet.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "PersistenceContextNotInManagedComponent");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, notInManagedComponentDiag);
    }

    /**
     * Plain class with two @PersistenceContext fields — each field must independently
     * produce a diagnostic.
     * Spec §7.6: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextMultipleFieldsInPlainClassInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextMultipleFieldsInPlainClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // "emA" at 0-based line 11, col 26-29
        Diagnostic emANotInManagedComponentDiag = d(11, 26, 29,
                                                    "@PersistenceContext can only be used in a container-managed component such as a CDI bean, EJB, or Servlet.",
                                                    DiagnosticSeverity.Error, "jakarta-persistence", "PersistenceContextNotInManagedComponent");

        // "emB" at 0-based line 14, col 26-29
        Diagnostic emBNotInManagedComponentDiag = d(14, 26, 29,
                                                    "@PersistenceContext can only be used in a container-managed component such as a CDI bean, EJB, or Servlet.",
                                                    DiagnosticSeverity.Error, "jakarta-persistence", "PersistenceContextNotInManagedComponent");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, emANotInManagedComponentDiag, emBNotInManagedComponentDiag);
    }

    /**
     * Tests that {@code @PersistenceContext} at the <em>type</em> level on a plain (unmanaged)
     * Java class produces a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextOnTypeInPlainClassInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextOnTypeInPlainClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Class name "PersistenceContextOnTypeInPlainClass" is at 0-based line 8,
        // starting at column 13 (after "public class "), length 36.
        Diagnostic notInManagedComponentDiag = d(8, 13, 49,
                                                 "@PersistenceContext can only be used in a container-managed component such as a CDI bean, EJB, or Servlet.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "PersistenceContextNotInManagedComponent");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, notInManagedComponentDiag);
    }

    /**
     * Tests that {@code @PersistenceContext} at the <em>type</em> level on a CDI
     * {@code @RequestScoped} bean does NOT produce a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextOnTypeInCdiBeanValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextOnTypeInCdiBeanValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — class is annotated with @RequestScoped (CDI managed bean)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that {@code @PersistenceContext} at the <em>method</em> level on a plain (unmanaged)
     * Java class produces a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextOnMethodInPlainClassInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextOnMethodInPlainClass.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Method name "setEntityManager" is at 0-based line 13,
        // starting at column 16 (after "    public void "), length 16.
        Diagnostic notInManagedComponentDiag = d(13, 16, 32,
                                                 "@PersistenceContext can only be used in a container-managed component such as a CDI bean, EJB, or Servlet.",
                                                 DiagnosticSeverity.Error, "jakarta-persistence", "PersistenceContextNotInManagedComponent");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, notInManagedComponentDiag);
    }

    /**
     * Tests that {@code @PersistenceContext} at the <em>method</em> level on an EJB
     * {@code @Stateless} session bean does NOT produce a diagnostic.
     *
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
     */
    @Test
    public void persistenceContextOnMethodInStatelessValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextOnMethodInStatelessValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — class is annotated with @Stateless (EJB session bean)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    // -------------------------------------------------------------------------
    // §7.6.3 — PersistenceContextType.EXTENDED only valid in @Stateful EJB
    // -------------------------------------------------------------------------

    /**
     * Tests that @PersistenceContext(type = EXTENDED) in a @Stateful EJB does NOT produce a diagnostic.
     *
     * §7.6.3: A container-managed EXTENDED persistence context can only be initiated within a
     * stateful session bean. @Stateful is the sole valid container type for EXTENDED.
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11810
     */
    @Test
    public void persistenceContextExtendedInStatefulValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextExtendedInStateful.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected — EXTENDED context in @Stateful EJB is explicitly valid per §7.6.3
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /**
     * Tests that @PersistenceContext(type = EXTENDED) in a @Stateless EJB produces a diagnostic.
     *
     * §7.6.3: EXTENDED context can only be initiated within a @Stateful session bean.
     * Spec ref: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11810
     */
    @Test
    public void persistenceContextExtendedInStatelessInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextExtendedInStateless.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Field "em" at 0-based line 15, columns 26-28.
        Diagnostic extendedInStatelessDiag = d(15, 26, 28,
                                               "PersistenceContextType.EXTENDED can only be used in a @Stateful EJB session bean.",
                                               DiagnosticSeverity.Error, "jakarta-persistence", "ExtendedPersistenceContextInNonStatefulBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, extendedInStatelessDiag);
    }

    /**
     * @PersistenceContext(type=EXTENDED) in a @Singleton EJB — invalid per §7.6.3.
     *                                    EXTENDED context can only be initiated within a @Stateful session bean.
     */
    @Test
    public void persistenceContextExtendedInSingletonInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextExtendedInSingleton.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // "em" at 0-based line 14, col 26-28
        Diagnostic extendedInSingletonDiag = d(14, 26, 28,
                                               "PersistenceContextType.EXTENDED can only be used in a @Stateful EJB session bean.",
                                               DiagnosticSeverity.Error, "jakarta-persistence", "ExtendedPersistenceContextInNonStatefulBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, extendedInSingletonDiag);
    }

    /**
     * @PersistenceContext(type=EXTENDED) in a CDI @ApplicationScoped bean — invalid per §7.6.3.
     *                                    EXTENDED is an EJB-only concept; CDI beans cannot use it.
     */
    @Test
    public void persistenceContextExtendedInCdiBeanInvalid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextExtendedInCdiBean.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // "em" at 0-based line 14, col 26-28
        Diagnostic extendedInCdiBeanDiag = d(14, 26, 28,
                                             "PersistenceContextType.EXTENDED can only be used in a @Stateful EJB session bean.",
                                             DiagnosticSeverity.Error, "jakarta-persistence", "ExtendedPersistenceContextInNonStatefulBean");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, extendedInCdiBeanDiag);
    }

    /** @Stateful EJB with default (TRANSACTION) type — no diagnostic expected. */
    @Test
    public void persistenceContextTransactionInStatefulValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextTransactionInStateful.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics: @Stateful is a managed component and default type is TRANSACTION
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    /** @Singleton EJB with default (TRANSACTION) type — no diagnostic expected. */
    @Test
    public void persistenceContextInSingletonValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/persistence/context/PersistenceContextInSingletonValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics: @Singleton is a managed component and TRANSACTION type is not restricted
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }
}
