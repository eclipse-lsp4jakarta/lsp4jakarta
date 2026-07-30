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

public class SessionBeanFinalizeTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void testInvalidStatelessBeanFinalize() throws Exception {
        assertFinalizeMethodDiagnostic("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidStatelessBeanFinalize.java",
                                       27);
    }

    @Test
    public void testInvalidStatefulBeanFinalize() throws Exception {
        assertFinalizeMethodDiagnostic("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidStatefulBeanFinalize.java",
                                       27);
    }

    @Test
    public void testInvalidSingletonBeanFinalize() throws Exception {
        assertFinalizeMethodDiagnostic("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidSingletonBeanFinalize.java",
                                       27);
    }

    @Test
    public void testValidStatelessBeanNoFinalize() throws Exception {
        assertNoDiagnostics("src/main/java/io/openliberty/sample/jakarta/ejb/ValidStatelessBeanNoFinalize.java");
    }

    private void assertFinalizeMethodDiagnostic(String projectRelativePath, int endCharacter) throws Exception {
        String uri = getJavaFileUri(projectRelativePath);
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic finalizeDiagnostic = d(22, 19, endCharacter,
                                          "Session beans must not override or define the finalize() method.",
                                          DiagnosticSeverity.Error, "jakarta-ejb", "SessionBeanFinalizeMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, finalizeDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, finalizeDiagnostic);
        TextEdit removeMethodEdit = te(19, 5, 25, 5, "");
        CodeAction removeMethodAction = ca(uri, "Remove finalize() method", finalizeDiagnostic, removeMethodEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, removeMethodAction);
    }

    private void assertNoDiagnostics(String projectRelativePath) throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getJavaFileUri(projectRelativePath)), IJDT_UTILS);
    }

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
