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
    // Diagnostic: @AfterCompletion method must be of type void
    // -----------------------------------------------------------------------

    @Test
    public void testNonVoidSessionSyncMethodDiagnosticAndQuickFix() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidNonVoidSessionSyncMethod.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // Line 13 (0-based): "    public boolean afterComplete(boolean committed) {"
        // "    public boolean " = 19 chars -> method name starts at col 19
        // "afterComplete" is 13 chars -> ends at col 32
        Diagnostic nonVoidDiagnostic = d(13, 19, 32,
                                         "@AfterCompletion session synchronization method must be of type void.",
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
    // Diagnostic: @AfterBegin method must not declare any parameters
    // -----------------------------------------------------------------------

    @Test
    public void testAfterBeginWithParamsDiagnosticAndQuickFix() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidNoParamAfterBegin.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // Line 13 (0-based): "    public void afterBegin(String info) {"
        // "    public void " = 16 chars -> method name starts at col 16
        // "afterBegin" is 10 chars -> ends at col 26
        Diagnostic afterBeginWithParamsDiagnostic = d(13, 16, 26,
                                                      "@AfterBegin session synchronization method must not declare any parameters.",
                                                      DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodNoParamAnnotation");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, afterBeginWithParamsDiagnostic);

        // QuickFix: remove all parameters
        // Line 13: "    public void afterBegin(String info) {"
        // open paren at col 26, "String info" from col 27 to col 38
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, afterBeginWithParamsDiagnostic);
        TextEdit removeParamsEdit = te(13, 27, 13, 38, "");
        CodeAction removeParamsAction = ca(uri, "Remove all parameters", afterBeginWithParamsDiagnostic, removeParamsEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeParamsAction);
    }

    // -----------------------------------------------------------------------
    // Diagnostic: @BeforeCompletion method must not declare any parameters
    // -----------------------------------------------------------------------

    @Test
    public void testBeforeCompletionWithParamsDiagnosticAndQuickFix() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidNoParamBeforeCompletion.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // Line 13 (0-based): "    public void beforeCompletion(int status) {"
        // "    public void " = 16 chars -> method name starts at col 16
        // "beforeCompletion" is 16 chars -> ends at col 32
        Diagnostic beforeCompletionWithParamsDiagnostic = d(13, 16, 32,
                                                            "@BeforeCompletion session synchronization method must not declare any parameters.",
                                                            DiagnosticSeverity.Error, "jakarta-ejb", "InvalidSessionSyncMethodNoParamAnnotation");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, beforeCompletionWithParamsDiagnostic);

        // QuickFix: remove all parameters
        // Line 13: "    public void beforeCompletion(int status) {"
        // open paren at col 32, "int status" from col 33 to col 43
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, beforeCompletionWithParamsDiagnostic);
        TextEdit removeParamsEdit = te(13, 33, 13, 43, "");
        CodeAction removeParamsAction = ca(uri, "Remove all parameters", beforeCompletionWithParamsDiagnostic, removeParamsEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeParamsAction);
    }

    // -----------------------------------------------------------------------
    // Diagnostic: @AfterCompletion method must have exactly one boolean parameter
    // -----------------------------------------------------------------------

    @Test
    public void testAfterCompletionMissingParamDiagnostic() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidAfterCompletionNoParam.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // Line 14 (0-based): "    public void afterCompletion() {"
        // "    public void " = 16 chars -> method name starts at col 16
        // "afterCompletion" is 15 chars -> ends at col 31
        Diagnostic afterCompletionMissingParamDiagnostic = d(14, 16, 31,
                                                             "@AfterCompletion session synchronization method must declare exactly one boolean parameter.",
                                                             DiagnosticSeverity.Error, "jakarta-ejb", "InvalidAfterCompletionMethodParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, afterCompletionMissingParamDiagnostic);
    }

    @Test
    public void testAfterCompletionWrongParamTypeDiagnostic() throws Exception {
        String uri = getJavaFileUri(BASE_PATH + "InvalidAfterCompletionWrongParam.java");
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        // Line 14 (0-based): "    public void afterCompletion(String status) {"
        // "    public void " = 16 chars -> method name starts at col 16
        // "afterCompletion" is 15 chars -> ends at col 31
        Diagnostic afterCompletionWrongParamDiagnostic = d(14, 16, 31,
                                                           "@AfterCompletion session synchronization method must declare exactly one boolean parameter.",
                                                           DiagnosticSeverity.Error, "jakarta-ejb", "InvalidAfterCompletionMethodParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, afterCompletionWrongParamDiagnostic);
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
