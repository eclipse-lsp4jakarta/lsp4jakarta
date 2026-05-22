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

public class MultipleObserverParamsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void multipleObserverParams() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/MultipleObserverParams.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Invalid: Two parameters, each with @Observes
        Diagnostic twoObserves = d(15, 16, 34,
                                   "Parameters event1, event2 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        // Invalid: One parameter with @Observes, another with @ObservesAsync
        Diagnostic observesAndObservesAsync = d(19, 16, 47,
                                                "Parameters event1, event2 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        // Invalid: Three parameters - one with @Observes, two with @ObservesAsync
        Diagnostic threeObserves = d(23, 16, 36,
                                     "Parameters event1, event2, event3 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, twoObserves, observesAndObservesAsync, threeObserves);

        JakartaJavaCodeActionParams twoObservesCodeActionParams = createCodeActionParams(uri, twoObserves);
        TextEdit removeEvent1ObservesEdit = te(15, 35, 15, 45, "");
        CodeAction removeTwoObservesEvent1Action = ca(uri, "Remove the '@Observes' modifier from parameter 'event1'", twoObserves, removeEvent1ObservesEdit);
        TextEdit removeEvent2ObservesEdit = te(15, 60, 15, 70, "");
        CodeAction removeTwoObservesEvent2Action = ca(uri, "Remove the '@Observes' modifier from parameter 'event2'", twoObserves, removeEvent2ObservesEdit);

        assertJavaCodeAction(twoObservesCodeActionParams, IJDT_UTILS, removeTwoObservesEvent1Action, removeTwoObservesEvent2Action);
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, observesAndObservesAsync);
        TextEdit removeEvent1ObservesEdit2 = te(19, 48, 19, 58, "");
        CodeAction removeMixedObservesEvent1Action = ca(uri, "Remove the '@Observes' modifier from parameter 'event1'", observesAndObservesAsync, removeEvent1ObservesEdit2);
        TextEdit removeEvent2ObservesAsyncEdit = te(19, 73, 19, 88, "");
        CodeAction removeMixedObservesAsyncEvent2Action = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'event2'", observesAndObservesAsync,
                                                             removeEvent2ObservesAsyncEdit);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, removeMixedObservesEvent1Action, removeMixedObservesAsyncEvent2Action);

        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, threeObserves);
        TextEdit removeEvent1ObservesEdit3 = te(23, 37, 23, 47, "");
        CodeAction removeThreeObservesEvent1Action = ca(uri, "Remove the '@Observes' modifier from parameter 'event1'", threeObserves, removeEvent1ObservesEdit3);
        TextEdit removeEvent2ObservesAsyncEdit2 = te(23, 62, 23, 77, "");
        CodeAction removeThreeObservesAsyncEvent2Action = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'event2'", threeObserves, removeEvent2ObservesAsyncEdit2);
        TextEdit removeEvent3ObservesAsyncEdit = te(23, 92, 23, 107, "");
        CodeAction removeThreeObservesAsyncEvent3Action = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'event3'", threeObserves, removeEvent3ObservesAsyncEdit);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, removeThreeObservesEvent1Action, removeThreeObservesAsyncEvent2Action, removeThreeObservesAsyncEvent3Action);
    }
}
