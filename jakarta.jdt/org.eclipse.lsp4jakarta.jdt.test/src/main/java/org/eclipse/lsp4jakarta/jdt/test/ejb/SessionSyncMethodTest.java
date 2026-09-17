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

        Diagnostic finalDiagnostic = d(13, 22, 31,
                                       "@AfterBegin session synchronization method must not be declared as final.",
                                       DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodFinal");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, finalDiagnostic);

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

        Diagnostic staticDiagnostic = d(13, 23, 35,
                                        "@BeforeCompletion session synchronization method must not be declared as static.",
                                        DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodStatic");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, staticDiagnostic);
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, staticDiagnostic);
        TextEdit removeStaticEdit = te(13, 10, 13, 17, "");
        CodeAction removeStaticAction = ca(uri, "Remove the 'static' modifier", staticDiagnostic, removeStaticEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeStaticAction);
    }

    // -----------------------------------------------------------------------
    // Diagnostic: @AfterCompletion method must be of type void
    // -----------------------------------------------------------------------

    @Test
    public void testNonVoidSessionSyncMethodDiagnosticAndQuickFix() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidNonVoidSessionSyncMethod.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic nonVoidDiagnostic = d(13, 19, 32,
                                         "@AfterCompletion session synchronization method must be of type void.",
                                         DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodNonVoid");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, nonVoidDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, nonVoidDiagnostic);
        TextEdit changeReturnTypeEdit = te(13, 11, 13, 18, "void");
        CodeAction changeReturnTypeAction = ca(uri, "Change return type to void", nonVoidDiagnostic, changeReturnTypeEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, changeReturnTypeAction);
    }

    // -----------------------------------------------------------------------
    // Mixed modifiers: multiple violations on the same method
    // -----------------------------------------------------------------------

    @Test
    public void testMixedModifiersSessionSyncMethodDiagnosticsAndQuickFixes() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidMixedModifiersSessionSyncMethod.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic mixedFinalDiagnostic = d(16, 29, 43,
                                            "@AfterBegin session synchronization method must not be declared as final.",
                                            DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodFinal");
        Diagnostic mixedStaticDiagnostic1 = d(16, 29, 43,
                                              "@AfterBegin session synchronization method must not be declared as static.",
                                              DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodStatic");

        Diagnostic mixedStaticDiagnostic2 = d(21, 26, 43,
                                              "@BeforeCompletion session synchronization method must not be declared as static.",
                                              DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodStatic");
        Diagnostic mixedNonVoidDiagnostic1 = d(21, 26, 43,
                                               "@BeforeCompletion session synchronization method must be of type void.",
                                               DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodNonVoid");

        Diagnostic mixedFinalDiagnostic2 = d(27, 21, 39,
                                             "@AfterCompletion session synchronization method must not be declared as final.",
                                             DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodFinal");
        Diagnostic mixedNonVoidDiagnostic2 = d(27, 21, 39,
                                               "@AfterCompletion session synchronization method must be of type void.",
                                               DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodNonVoid");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              mixedFinalDiagnostic, mixedStaticDiagnostic1,
                              mixedStaticDiagnostic2, mixedNonVoidDiagnostic1,
                              mixedFinalDiagnostic2, mixedNonVoidDiagnostic2);

        JakartaJavaCodeActionParams finalParams1 = createCodeActionParams(uri, mixedFinalDiagnostic);
        TextEdit removeFinal1 = te(16, 17, 16, 23, "");
        CodeAction removeFinalAction1 = ca(uri, "Remove the 'final' modifier", mixedFinalDiagnostic, removeFinal1);
        assertJavaCodeAction(finalParams1, IJDT_UTILS, removeFinalAction1);

        // QuickFix for beginSyncMixed - remove 'static': "static " occupies cols [11, 18)
        // (AST uses trailing space when 'static' precedes another modifier)
        JakartaJavaCodeActionParams staticParams1 = createCodeActionParams(uri, mixedStaticDiagnostic1);
        TextEdit removeStatic1 = te(16, 11, 16, 18, "");
        CodeAction removeStaticAction1 = ca(uri, "Remove the 'static' modifier", mixedStaticDiagnostic1, removeStatic1);
        assertJavaCodeAction(staticParams1, IJDT_UTILS, removeStaticAction1);

        // QuickFix for beforeCommitMixed - remove 'static': " static" occupies cols [10, 17)
        JakartaJavaCodeActionParams staticParams2 = createCodeActionParams(uri, mixedStaticDiagnostic2);
        TextEdit removeStatic2 = te(21, 10, 21, 17, "");
        CodeAction removeStaticAction2 = ca(uri, "Remove the 'static' modifier", mixedStaticDiagnostic2, removeStatic2);
        assertJavaCodeAction(staticParams2, IJDT_UTILS, removeStaticAction2);

        // QuickFix for beforeCommitMixed - change return type to void: "boolean" occupies cols [18, 25)
        JakartaJavaCodeActionParams nonVoidParams1 = createCodeActionParams(uri, mixedNonVoidDiagnostic1);
        TextEdit changeReturnType1 = te(21, 18, 21, 25, "void");
        CodeAction changeReturnTypeAction1 = ca(uri, "Change return type to void", mixedNonVoidDiagnostic1, changeReturnType1);
        assertJavaCodeAction(nonVoidParams1, IJDT_UTILS, changeReturnTypeAction1);

        // QuickFix for afterCompleteMixed - remove 'final': " final" occupies cols [10, 16)
        JakartaJavaCodeActionParams finalParams2 = createCodeActionParams(uri, mixedFinalDiagnostic2);
        TextEdit removeFinal2 = te(27, 10, 27, 16, "");
        CodeAction removeFinalAction2 = ca(uri, "Remove the 'final' modifier", mixedFinalDiagnostic2, removeFinal2);
        assertJavaCodeAction(finalParams2, IJDT_UTILS, removeFinalAction2);

        // QuickFix for afterCompleteMixed - change return type to void: "int" occupies cols [17, 20)
        JakartaJavaCodeActionParams nonVoidParams2 = createCodeActionParams(uri, mixedNonVoidDiagnostic2);
        TextEdit changeReturnType2 = te(27, 17, 27, 20, "void");
        CodeAction changeReturnTypeAction2 = ca(uri, "Change return type to void", mixedNonVoidDiagnostic2, changeReturnType2);
        assertJavaCodeAction(nonVoidParams2, IJDT_UTILS, changeReturnTypeAction2);
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
    // Negative: non-session-bean class produces no diagnostics even with
    // session sync annotations that would otherwise violate the spec
    // -----------------------------------------------------------------------

    @Test
    public void testNonSessionBeanWithSessionSyncMethodsProducesNoDiagnostics() throws Exception {
        assertJavaDiagnostics(
                              createDiagnosticsParams(getJavaFileUri(BASE_PATH + "NonSessionBeanWithSessionSyncMethods.java")),
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
