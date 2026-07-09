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

public class SessionBeanModifierTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    /**
     * InvalidSessionBeanModifiers.java (all lines 0-based):
     *
     * Line 14: "class NotPublicStatelessBean {"
     * name col 6..28 → InvalidModifierNotPublic
     *
     * Line 21: "public final class FinalStatelessBean {"
     * name col 19..37 → InvalidModifierFinal
     *
     * Line 28: "abstract class AbstractStatefulBean {"
     * name col 15..35 → InvalidModifierAbstract
     *
     * Line 35: "final class FinalNotPublicSingletonBean {"
     * name col 12..39 → InvalidModifierNotPublic + InvalidModifierFinal
     */
    @Test
    public void testInvalidSessionBeanModifiers() throws Exception {
        String uri = getFileUri("InvalidSessionBeanModifiers.java");
        JakartaJavaDiagnosticsParams params = createDiagnosticsParams(uri);

        // NotPublicStatelessBean — "class " = 6 chars, name 22 chars
        Diagnostic notPublic = d(14, 6, 28,
                                 "A session bean class must be declared public.",
                                 DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierNotPublic");

        // FinalStatelessBean — "public final class " = 19 chars, name 18 chars
        Diagnostic isFinal = d(21, 19, 37,
                               "A session bean class must not be declared final.",
                               DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierFinal");

        // AbstractStatefulBean — "abstract class " = 15 chars, name 20 chars
        // also not public ("abstract class " has no "public" prefix)
        Diagnostic abstractNotPublic = d(28, 15, 35,
                                         "A session bean class must be declared public.",
                                         DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierNotPublic");
        Diagnostic isAbstract = d(28, 15, 35,
                                  "A session bean class must not be declared abstract.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierAbstract");

        // FinalNotPublicSingletonBean — "final class " = 12 chars, name 27 chars
        Diagnostic finalNotPublic = d(35, 12, 39,
                                      "A session bean class must be declared public.",
                                      DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierNotPublic");
        Diagnostic finalIsFinal = d(35, 12, 39,
                                    "A session bean class must not be declared final.",
                                    DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierFinal");

        assertJavaDiagnostics(params, IJDT_UTILS,
                              notPublic, isFinal, abstractNotPublic, isAbstract, finalNotPublic, finalIsFinal);
    }

    /**
     * NestedSessionBeanWrapper.java (0-based):
     *
     * Line 12: " public class NestedStatefulBean {"
     * " public class " = 18 chars, name 18 chars → col 18..36
     * → InvalidNotTopLevelClass
     */
    @Test
    public void testNestedSessionBeanNotTopLevel() throws Exception {
        String uri = getFileUri("NestedSessionBeanWrapper.java");
        JakartaJavaDiagnosticsParams params = createDiagnosticsParams(uri);

        // "    public class " = 4+6+1+5+1 = 17 chars — actual measured from run output
        Diagnostic notTopLevel = d(12, 17, 35,
                                   "A session bean class must be a top-level class.",
                                   DiagnosticSeverity.Error, "jakarta-ejb", "InvalidNotTopLevelClass");

        assertJavaDiagnostics(params, IJDT_UTILS, notTopLevel);
    }

    /**
     * ValidSessionBeans.java — no diagnostics expected for any of the three
     * valid session bean classes in the file.
     */
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
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/ejb/" + fileName));
        return javaFile.getLocation().toFile().toURI().toString();
    }
}
