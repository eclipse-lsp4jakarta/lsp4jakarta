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
 * Tests for CDI raw {@code Event} type injection point diagnostic and quickfix.
 *
 * <p>According to CDI specification section 10.4.2:
 * "If an injection point of raw type Event is defined, the container automatically
 * detects the problem and treats it as a definition error."
 *
 * <p>Tests cover:
 * <ul>
 * <li>Raw {@code Event} {@code @Inject} field — diagnostic + quickfix</li>
 * <li>Parameterized {@code Event<T>} field — no diagnostic (valid)</li>
 * <li>Raw {@code Event} method parameter — diagnostic on method name + quickfix</li>
 * <li>Parameterized {@code Event<T>} method parameter — no diagnostic (valid)</li>
 * <li>Mixed params with one raw {@code Event} — one diagnostic per method</li>
 * <li>Multiple raw {@code Event} params in one method — one diagnostic (not per-param)</li>
 * <li>Raw {@code Event} field without {@code @Inject} — no diagnostic</li>
 * <li>Method returning raw {@code Event} — no diagnostic (not an injection point)</li>
 * <li>Raw {@code Event} field in a nested class — diagnostic fires</li>
 * </ul>
 */
public class CdiRawEventTypeTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    private static final String REMOVE_INJECT = "Remove @Inject";

    @Test
    public void rawEventInjectionPointDiagnostics() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/RawEventInjectionPoint.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // --- Invalid cases ---

        // Line 18 (0-based 17): "    Event rawEvent;" — field name col 10-18
        Diagnostic rawEventFieldDiagnostic = d(17, 10, 18,
                                               "An injection point of raw type Event is not valid. Specify a type parameter, for example Event<String> or Event<MyType>.",
                                               DiagnosticSeverity.Error, "jakarta-cdi", "InvalidRawEventTypeInjectionPoint");

        // Line 33 (0-based 32): "    public void setRawEvent(Event event) {" — method name col 16-27
        Diagnostic rawEventMethodParamDiagnostic = d(32, 16, 27,
                                                     "An injection point of raw type Event is not valid. Specify a type parameter, for example Event<String> or Event<MyType>.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidRawEventTypeInjectionPoint");

        // Line 43 (0-based 42): "    public void setMixed(String name, Event rawEvent) {" — method name col 16-24
        // Edge case: mixed params (one valid, one raw Event) — one diagnostic on the method
        Diagnostic rawEventMixedParamDiagnostic = d(42, 16, 24,
                                                    "An injection point of raw type Event is not valid. Specify a type parameter, for example Event<String> or Event<MyType>.",
                                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidRawEventTypeInjectionPoint");

        // Line 48 (0-based 47): "    public void setMultipleRawEvents(Event first, Event second) {" — method name col 16-36
        // Edge case: two raw Event params — only ONE diagnostic per method (not one per param)
        Diagnostic rawEventMultipleParamsDiagnostic = d(47, 16, 36,
                                                        "An injection point of raw type Event is not valid. Specify a type parameter, for example Event<String> or Event<MyType>.",
                                                        DiagnosticSeverity.Error, "jakarta-cdi", "InvalidRawEventTypeInjectionPoint");

        // Line 60 (0-based 59): "        Event rawEventInInner;" inside nested class Inner — field name col 14-29
        // Edge case: nested class injection point is also flagged
        Diagnostic rawEventNestedClassDiagnostic = d(59, 14, 29,
                                                     "An injection point of raw type Event is not valid. Specify a type parameter, for example Event<String> or Event<MyType>.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidRawEventTypeInjectionPoint");

        // Assert all 5 diagnostics; valid cases produce no additional diagnostics:
        //   - Event<String> and Event<OrderCreated> fields — valid
        //   - Event notInjectedRawEvent (no @Inject) — not an injection point
        //   - setTypedEvent(Event<String>) method — valid
        //   - produceRawEvent() return type — not an injection point
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS,
                              rawEventFieldDiagnostic,
                              rawEventMethodParamDiagnostic,
                              rawEventMixedParamDiagnostic,
                              rawEventMultipleParamsDiagnostic,
                              rawEventNestedClassDiagnostic);

        // --- Quickfixes: each invalid injection point offers "Remove @Inject" ---

        // Field rawEvent: @Inject is line 17 (0-based 16), indent col 4
        JakartaJavaCodeActionParams fieldCodeActionParams = createCodeActionParams(uri, rawEventFieldDiagnostic);
        TextEdit removeFieldInjectEdit = te(16, 4, 17, 4, "");
        CodeAction removeFieldInjectAction = ca(uri, REMOVE_INJECT, rawEventFieldDiagnostic, removeFieldInjectEdit);
        assertJavaCodeAction(fieldCodeActionParams, IJDT_UTILS, removeFieldInjectAction);

        // Method setRawEvent: @Inject is line 32 (0-based 31), indent col 4
        JakartaJavaCodeActionParams methodCodeActionParams = createCodeActionParams(uri, rawEventMethodParamDiagnostic);
        TextEdit removeMethodInjectEdit = te(31, 4, 32, 4, "");
        CodeAction removeMethodInjectAction = ca(uri, REMOVE_INJECT, rawEventMethodParamDiagnostic, removeMethodInjectEdit);
        assertJavaCodeAction(methodCodeActionParams, IJDT_UTILS, removeMethodInjectAction);

        // Method setMixed: @Inject is line 43 (0-based 41), indent col 4
        JakartaJavaCodeActionParams mixedCodeActionParams = createCodeActionParams(uri, rawEventMixedParamDiagnostic);
        TextEdit removeMixedInjectEdit = te(41, 4, 42, 4, "");
        CodeAction removeMixedInjectAction = ca(uri, REMOVE_INJECT, rawEventMixedParamDiagnostic, removeMixedInjectEdit);
        assertJavaCodeAction(mixedCodeActionParams, IJDT_UTILS, removeMixedInjectAction);

        // Method setMultipleRawEvents: @Inject is line 48 (0-based 46), indent col 4
        JakartaJavaCodeActionParams multipleParamsCodeActionParams = createCodeActionParams(uri, rawEventMultipleParamsDiagnostic);
        TextEdit removeMultipleParamsInjectEdit = te(46, 4, 47, 4, "");
        CodeAction removeMultipleParamsInjectAction = ca(uri, REMOVE_INJECT, rawEventMultipleParamsDiagnostic, removeMultipleParamsInjectEdit);
        assertJavaCodeAction(multipleParamsCodeActionParams, IJDT_UTILS, removeMultipleParamsInjectAction);

        // Nested class Inner.rawEventInInner: @Inject is line 60 (0-based 58), indent col 8
        JakartaJavaCodeActionParams nestedCodeActionParams = createCodeActionParams(uri, rawEventNestedClassDiagnostic);
        TextEdit removeNestedInjectEdit = te(58, 8, 59, 8, "");
        CodeAction removeNestedInjectAction = ca(uri, REMOVE_INJECT, rawEventNestedClassDiagnostic, removeNestedInjectEdit);
        assertJavaCodeAction(nestedCodeActionParams, IJDT_UTILS, removeNestedInjectAction);
    }
}
