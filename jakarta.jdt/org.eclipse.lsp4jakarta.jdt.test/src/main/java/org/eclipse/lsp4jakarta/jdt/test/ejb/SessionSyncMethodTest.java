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
 * Tests for EJB session synchronization method diagnostics and quickfixes.
 *
 * Validates that methods annotated with @AfterBegin, @BeforeCompletion, or
 *
 * @AfterCompletion must not be declared final or static, and must return void.
 */
public class SessionSyncMethodTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    private static final String BASE_PATH = "src/main/java/io/openliberty/sample/jakarta/ejb/session_synchronization_method/";

    // -----------------------------------------------------------------------
    // Diagnostic: @AfterBegin method must not be final
    // -----------------------------------------------------------------------

    @Test
    public void testFinalSessionSyncMethodDiagnosticAndQuickFix() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidFinalSessionSyncMethod.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // Line 13 (0-based): "    public final void beginSync() {"
        // "    public final void " = 22 chars -> method name starts at col 22
        // "beginSync" is 9 chars -> ends at col 31
        Diagnostic finalDiagnostic = d(13, 22, 31,
                                       "@AfterBegin session synchronization method must not be declared as final.",
                                       DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodFinal");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, finalDiagnostic);

        // QuickFix: remove the 'final' modifier
        // Line 13: "    public final void beginSync() {"
        // AST modifier removal includes leading space: " final" -> edit range [10, 16)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, finalDiagnostic);
        TextEdit removeFinalEdit = te(13, 10, 13, 16, "");
        CodeAction removeFinalAction = ca(uri, "Remove the 'final' modifier", finalDiagnostic, removeFinalEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeFinalAction);
    }

    // -----------------------------------------------------------------------
    // Diagnostic: @BeforeCompletion method must not be static
    // -----------------------------------------------------------------------

    @Test
    public void testStaticSessionSyncMethodDiagnosticAndQuickFix() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidStaticSessionSyncMethod.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // Line 13 (0-based): "    public static void beforeCommit() {"
        // "    public static void " = 23 chars -> method name starts at col 23
        // "beforeCommit" is 12 chars -> ends at col 35
        Diagnostic staticDiagnostic = d(13, 23, 35,
                                        "@BeforeCompletion session synchronization method must not be declared as static.",
                                        DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodStatic");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, staticDiagnostic);

        // QuickFix: remove the 'static' modifier
        // Line 13: "    public static void beforeCommit() {"
        // AST modifier removal includes leading space: " static" -> edit range [10, 17)
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, staticDiagnostic);
        TextEdit removeStaticEdit = te(13, 10, 13, 17, "");
        CodeAction removeStaticAction = ca(uri, "Remove the 'static' modifier", staticDiagnostic, removeStaticEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeStaticAction);
    }

    // -----------------------------------------------------------------------
    // Diagnostic: @AfterCompletion method must return void
    // -----------------------------------------------------------------------

    @Test
    public void testNonVoidSessionSyncMethodDiagnosticAndQuickFix() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidNonVoidSessionSyncMethod.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // Line 13 (0-based): "    public boolean afterComplete(boolean committed) {"
        // "    public boolean " = 19 chars -> method name starts at col 19
        // "afterComplete" is 13 chars -> ends at col 32
        Diagnostic nonVoidDiagnostic = d(13, 19, 32,
                                         "@AfterCompletion session synchronization method must return void.",
                                         DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodNonVoid");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, nonVoidDiagnostic);

        // QuickFix: change return type to void
        // Line 13: "    public boolean afterComplete(boolean committed) {"
        // "boolean" occupies cols 11-17 (inclusive), text edit replaces with "void"
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, nonVoidDiagnostic);
        TextEdit changeReturnTypeEdit = te(13, 11, 13, 18, "void");
        CodeAction changeReturnTypeAction = ca(uri, "Change return type to void", nonVoidDiagnostic, changeReturnTypeEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, changeReturnTypeAction);
    }

    // -----------------------------------------------------------------------
    // Negative: valid session synchronization methods produce no diagnostics
    // -----------------------------------------------------------------------

    @Test
    public void testValidSessionSyncMethodsProduceNoDiagnostics() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getJavaFileUri(BASE_PATH + "ValidSessionSyncMethods.java")),
                              IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private JakartaJavaDiagnosticsParams createDiagnosticsParams(String uri) {
        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        return diagnosticsParams;
    }

    private String getJavaFileUri(String projectRelativePath) throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path(projectRelativePath));
        return javaFile.getLocation().toFile().toURI().toString();
    }
}
