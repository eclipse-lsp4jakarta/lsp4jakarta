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

        // Quickfixes: "Change modifier to public" sorts before "Remove @Stateless" alphabetically.
        // ChangeModifierToPublicQuickFix inserts "\npublic" after the @Stateless token (col 10).
        JakartaJavaCodeActionParams params = createCodeActionParams(uri, d);
        TextEdit makePublic = te(5, 10, 5, 10, "\npublic");
        TextEdit removeAnnotation = te(5, 0, 6, 0, "");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Change modifier to public", d, makePublic),
                             ca(uri, "Remove @Stateless", d, removeAnnotation));
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

        // Quickfixes: "Remove @Stateless" sorts before "Remove the 'final' modifier" alphabetically.
        // ModifyModifiersProposal removes " final" (col 6..12 = space + "final").
        JakartaJavaCodeActionParams params = createCodeActionParams(uri, d);
        TextEdit removeAnnotation = te(5, 0, 6, 0, "");
        TextEdit removeFinal = te(6, 6, 6, 12, "");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Stateless", d, removeAnnotation),
                             ca(uri, "Remove the 'final' modifier", d, removeFinal));
    }

    // -----------------------------------------------------------------------
    // Invalid: abstract only (class is public)
    // File line 6 (0-based): "public abstract class AbstractStatefulBean {"
    //   "public "(7) + "abstract "(9) + "class "(6) = 22 chars → col 22..42
    // -----------------------------------------------------------------------
    @Test
    public void testSessionBeanMustNotBeAbstract() throws Exception {
        String uri = getFileUri("AbstractStatefulBean.java");
        Diagnostic isAbstract = d(6, 22, 42,
                                  "A session bean class must not be declared abstract.",
                                  DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierAbstract");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, isAbstract);

        // Quickfixes: "Remove @Stateful" sorts before "Remove the 'abstract' modifier" alphabetically.
        // ModifyModifiersProposal removes " abstract" (col 6..15 = space + "abstract").
        JakartaJavaCodeActionParams params = createCodeActionParams(uri, isAbstract);
        TextEdit removeAnnotation = te(5, 0, 6, 0, "");
        TextEdit removeAbstract = te(6, 6, 6, 15, "");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Stateful", isAbstract, removeAnnotation),
                             ca(uri, "Remove the 'abstract' modifier", isAbstract, removeAbstract));
    }

    // -----------------------------------------------------------------------
    // Invalid: final only (class is public)
    // File line 6 (0-based): "public final class FinalNotPublicSingletonBean {"
    //   "public final class " = 19 chars, name 27 chars → col 19..46
    // -----------------------------------------------------------------------
    @Test
    public void testSessionBeanMustNotBeFinal_Singleton() throws Exception {
        String uri = getFileUri("FinalNotPublicSingletonBean.java");
        Diagnostic isFinal = d(6, 19, 46,
                               "A session bean class must not be declared final.",
                               DiagnosticSeverity.Error, "jakarta-ejb", "InvalidModifierFinal");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, isFinal);

        // Quickfixes: "Remove @Singleton" sorts before "Remove the 'final' modifier" alphabetically.
        // ModifyModifiersProposal removes " final" (col 6..12 = space + "final").
        JakartaJavaCodeActionParams params = createCodeActionParams(uri, isFinal);
        TextEdit removeAnnotation = te(5, 0, 6, 0, "");
        TextEdit removeFinal = te(6, 6, 6, 12, "");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Singleton", isFinal, removeAnnotation),
                             ca(uri, "Remove the 'final' modifier", isFinal, removeFinal));
    }

    // -----------------------------------------------------------------------
    // Invalid: not a top-level class
    // NestedSessionBeanWrapper.java line 12 (0-based):
    //   "    public class NestedStatefulBean {"
    //   col 17..35 (measured from actual test run)
    // The @Stateful annotation is at line 11 (0-based), indented 4 spaces.
    // -----------------------------------------------------------------------
    @Test
    public void testSessionBeanMustBeTopLevel() throws Exception {
        String uri = getFileUri("NestedSessionBeanWrapper.java");
        Diagnostic d = d(12, 17, 35,
                         "A session bean class must be a top-level class.",
                         DiagnosticSeverity.Error, "jakarta-ejb", "InvalidNotTopLevelClass");
        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, d);

        // Quickfix: remove the @Stateful annotation (indented, so range is 4-space-offset line)
        JakartaJavaCodeActionParams params = createCodeActionParams(uri, d);
        TextEdit removeAnnotation = te(11, 4, 12, 4, "");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Stateful", d, removeAnnotation));
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
