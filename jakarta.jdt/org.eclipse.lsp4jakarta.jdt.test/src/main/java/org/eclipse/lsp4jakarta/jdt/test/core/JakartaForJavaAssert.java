/*******************************************************************************
* Copyright (c) 2020, 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.test.core;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.jdt.core.PropertiesManagerForJava;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.junit.Assert;

/**
 * Modified from:
 * https://github.com/eclipse/lsp4mp/blob/bc926f75df2ca103d78c67b997c87adb7ab480b1/microprofile.jdt/org.eclipse.lsp4mp.jdt.test/src/main/java/org/eclipse/lsp4mp/jdt/core/MicroProfileForJavaAssert.java
 * With certain methods modified or deleted to fit the purposes of LSP4Jakarta
 */
public class JakartaForJavaAssert {

    // ------------------- Assert for CodeAction

    public static JakartaJavaCodeActionParams createCodeActionParams(String uri, Diagnostic d) {
        TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
        Range range = d.getRange();
        CodeActionContext context = new CodeActionContext();
        context.setDiagnostics(Arrays.asList(d));
        JakartaJavaCodeActionParams codeActionParams = new JakartaJavaCodeActionParams(textDocument, range, context);
        codeActionParams.setResourceOperationSupported(true);
        return codeActionParams;
    }

    public static void assertJavaCodeAction(JakartaJavaCodeActionParams params, IJDTUtils utils, CodeAction... expected) throws JavaModelException {
        List<? extends CodeAction> actual = PropertiesManagerForJava.getInstance().codeAction(params, utils,
                                                                                              new NullProgressMonitor());
        assertCodeActions(actual != null && actual.size() > 0 ? actual : Collections.emptyList(), expected);
    }

    public static void assertCodeActions(List<? extends CodeAction> actual, CodeAction... expected) {
        actual.stream().forEach(ca -> {
            // we don't want to compare title, etc
            ca.setCommand(null);
            ca.setKind(null);
            if (ca.getDiagnostics() != null) {
                ca.getDiagnostics().forEach(d -> {
                    d.setSeverity(null);
                    d.setMessage("");
                    d.setSource(null);
                });
            }
        });

        Assert.assertEquals(expected.length, actual.size());

        // remove extra '\r'
        for (CodeAction ca : actual) {
            ca.setTitle(replaceNewLineCharacters(ca.getTitle()));
            List<Either<TextDocumentEdit, ResourceOperation>> tdes = ca.getEdit().getDocumentChanges();
            for (Either<TextDocumentEdit, ResourceOperation> tde : tdes) {
                List<TextEdit> tes = tde.getLeft().getEdits();
                for (TextEdit te : tes) {
                    te.setNewText(replaceNewLineCharacters(te.getNewText()));
                }
            }
        }

        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals("Assert title [" + i + "]", expected[i].getTitle(),
                                ((CodeAction) actual.get(i)).getTitle());
            Assert.assertEquals("Assert edit [" + i + "]", expected[i].getEdit(),
                                ((CodeAction) actual.get(i)).getEdit());
        }
    }

    public static CodeAction ca(String uri, String title, Diagnostic d, TextEdit... te) {
        CodeAction codeAction = new CodeAction();
        codeAction.setTitle(title);
        codeAction.setDiagnostics(Arrays.asList(d));

        VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(uri, 0);

        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(te));
        WorkspaceEdit workspaceEdit = new WorkspaceEdit(Arrays.asList(Either.forLeft(textDocumentEdit)));
        workspaceEdit.setChanges(Collections.emptyMap());
        codeAction.setEdit(workspaceEdit);
        return codeAction;
    }

    public static TextEdit te(int startLine, int startCharacter, int endLine, int endCharacter, String newText) {
        TextEdit textEdit = new TextEdit();
        textEdit.setNewText(newText);
        textEdit.setRange(r(startLine, startCharacter, endLine, endCharacter));
        return textEdit;
    }

    // Assert for diagnostics

    public static Diagnostic d(int line, int startCharacter, int endCharacter, String message,
                               DiagnosticSeverity severity, final String source, String code, Object data) {
        Diagnostic d = new Diagnostic(r(line, startCharacter, line, endCharacter), message, severity, source, code != null ? code : null);
        d.setData(data);
        return d;
    }

    public static Diagnostic d(int line, int startCharacter, int endCharacter, String message,
                               DiagnosticSeverity severity, final String source, String code) {
        return d(line, startCharacter, line, endCharacter, message, severity, source, code);
    }

    public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, String message,
                               DiagnosticSeverity severity, final String source, String code) {
        // Diagnostic on 1 line
        return new Diagnostic(r(startLine, startCharacter, endLine, endCharacter), message, severity, source, code != null ? code : null);
    }

    public static Range r(int line, int startCharacter, int endCharacter) {
        return r(line, startCharacter, line, endCharacter);
    }

    public static Range r(int startLine, int startCharacter, int endLine, int endCharacter) {
        return new Range(p(startLine, startCharacter), p(endLine, endCharacter));
    }

    public static Position p(int line, int character) {
        return new Position(line, character);
    }

    public static void assertJavaDiagnostics(JakartaJavaDiagnosticsParams params, IJDTUtils utils,
                                             Diagnostic... expected) throws JavaModelException {
        List<PublishDiagnosticsParams> actual = PropertiesManagerForJava.getInstance().diagnostics(params,
                                                                                                   utils, new NullProgressMonitor());

        assertDiagnostics(
                          actual != null && actual.size() > 0 ? actual.get(0).getDiagnostics() : Collections.emptyList(),
                          expected);
    }

    public static void assertDiagnostics(List<Diagnostic> actual, Diagnostic... expected) {
        assertDiagnostics(actual, Arrays.asList(expected), false);
    }

    public static void assertDiagnostics(List<Diagnostic> actual, List<Diagnostic> expected, boolean filter) {
        /**
         * ordering of diagnostics should not matter when testing for equality, so we
         * sort diagnostics by their range.
         */
        Comparator<Position> posOrder = (a, b) -> a.getLine() == b.getLine() ? b.getCharacter() - a.getCharacter() : b.getLine() - a.getLine();

        Comparator<Range> rangePosOrder = (a, b) -> posOrder.compare(a.getStart(), b.getStart()) == 0 ? posOrder.compare(a.getEnd(), b.getEnd()) : posOrder.compare(a.getStart(),
                                                                                                                                                                    b.getStart());

        Comparator<Diagnostic> diagnosticRangeOrder = (a, b) -> rangePosOrder.compare(a.getRange(), b.getRange());

        actual.sort(diagnosticRangeOrder);
        expected.sort(diagnosticRangeOrder);

        // probably don't have to do this, as the code is using same way to build
        // 'expected Diagnostic' object
        // remove extra '\r'
        for (Diagnostic dia : actual) {
            dia.setMessage(replaceNewLineCharacters(dia.getMessage()));
        }

        List<Diagnostic> received = actual;
        final boolean filterMessage;
        if (expected != null && !expected.isEmpty()
            && (expected.get(0).getMessage() == null || expected.get(0).getMessage().isEmpty())) {
            filterMessage = true;
        } else {
            filterMessage = false;
        }
        if (filter) {
            received = actual.stream().map(d -> {
                Diagnostic simpler = new Diagnostic(d.getRange(), "");
                simpler.setCode(d.getCode());
                if (filterMessage) {
                    simpler.setMessage(d.getMessage());
                }
                return simpler;
            }).collect(Collectors.toList());
        }
        Assert.assertEquals("Unexpected diagnostics:\n" + actual, expected, received);
    }

    public static String fixURI(URI uri) {
        String uriString = uri.toString();
        return uriString.replaceFirst("file:/([^/])", "file:///$1");
    }

    // util. APIs

    /**
     * Returns new string without '\r' for new lines. (Windows generates an extra
     * '\r' for each new line, it makes test messages fail to compare.)
     *
     * @param source test message string
     * @return new string without '\r' for new lines
     */
    public static String replaceNewLineCharacters(String source) {
        return source.replaceAll("\r\n", "\n");
    }
}