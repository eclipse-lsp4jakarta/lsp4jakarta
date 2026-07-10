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

package org.eclipse.lsp4jakarta.jdt.test.ejb;

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
 * Tests for Jakarta Enterprise Beans 4.0 spec section 4.1 class constraints:
 * session bean classes must be public, non-final, non-abstract, and top-level.
 *
 * Test resources are under:
 * io/openliberty/sample/jakarta/ejb/classconstraints/
 */
public class SessionBeanModifierTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -----------------------------------------------------------------------
    // Invalid: not public
    // File line 6 (0-based): "class NotPublicStatelessBean {"
    //   "class " = 6 chars, name 22 chars → col 6..28
    // -----------------------------------------------------------------------
    @Test
    public void testSessionBeanMustBePublic() throws Exception {
        String uri = getFileUri("NotPublicStatelessBean.java");
        Diagnostic d = d(6, 6, 28,
                         "A session bean class must be declared public.",
                         DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierNotPublic");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, d);
    }

    // -----------------------------------------------------------------------
    // Invalid: final
    // File line 6 (0-based): "public final class FinalStatelessBean {"
    //   "public final class " = 19 chars, name 18 chars → col 19..37
    // -----------------------------------------------------------------------
    @Test
    public void testSessionBeanMustNotBeFinal() throws Exception {
        String uri = getFileUri("FinalStatelessBean.java");
        Diagnostic d = d(6, 19, 37,
                         "A session bean class must not be declared final.",
                         DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierFinal");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, d);
    }

    // -----------------------------------------------------------------------
    // Invalid: abstract (also not public — two diagnostics)
    // File line 6 (0-based): "abstract class AbstractStatefulBean {"
    //   "abstract class " = 15 chars, name 20 chars → col 15..35
    // -----------------------------------------------------------------------
    @Test
    public void testSessionBeanMustNotBeAbstract() throws Exception {
        String uri = getFileUri("AbstractStatefulBean.java");
        Diagnostic notPublic = d(6, 15, 35,
                                 "A session bean class must be declared public.",
                                 DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierNotPublic");
        Diagnostic isAbstract = d(6, 15, 35,
                                  "A session bean class must not be declared abstract.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierAbstract");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, notPublic, isAbstract);
    }

    // -----------------------------------------------------------------------
    // Invalid: final and not public — two diagnostics
    // File line 6 (0-based): "final class FinalNotPublicSingletonBean {"
    //   "final class " = 12 chars, name 27 chars → col 12..39
    // -----------------------------------------------------------------------
    @Test
    public void testSessionBeanMustNotBeFinalAndMustBePublic() throws Exception {
        String uri = getFileUri("FinalNotPublicSingletonBean.java");
        Diagnostic notPublic = d(6, 12, 39,
                                 "A session bean class must be declared public.",
                                 DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierNotPublic");
        Diagnostic isFinal = d(6, 12, 39,
                               "A session bean class must not be declared final.",
                               DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierFinal");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, notPublic, isFinal);
    }

    // -----------------------------------------------------------------------
    // Invalid: not a top-level class
    // NestedSessionBeanWrapper.java line 12 (0-based):
    //   "    public class NestedStatefulBean {"
    //   col 17..35 (measured from actual test run)
    // -----------------------------------------------------------------------
    @Test
    public void testSessionBeanMustBeTopLevel() throws Exception {
        String uri = getFileUri("NestedSessionBeanWrapper.java");
        Diagnostic d = d(12, 17, 35,
                         "A session bean class must be a top-level class.",
                         DiagnosticSeverity.Error, "jakarta-ejb", "InvalidNotTopLevelClass");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, d);
    }

    // -----------------------------------------------------------------------
    // Valid: public, non-final, non-abstract, top-level — no diagnostics
    // -----------------------------------------------------------------------
    @Test
    public void testValidSessionBeans() throws Exception {
        String uri = getFileUri("ValidSessionBeans.java");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS);
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
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/ejb/classconstraints/" + fileName));
        return javaFile.getLocation().toFile().toURI().toString();
    }
}
