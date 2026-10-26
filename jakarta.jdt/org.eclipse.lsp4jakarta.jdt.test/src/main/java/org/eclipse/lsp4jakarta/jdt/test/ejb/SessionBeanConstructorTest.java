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

public class SessionBeanConstructorTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void testInvalidStatelessBeanPublic() throws Exception {
        assertMissingPublicNoArgConstructor("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidStatelessBeanPublic.java",
                                            39, "public InvalidStatelessBeanPublic() {\n\t}\n\n\t");
    }

    @Test
    public void testInvalidStatefulBeanPublic() throws Exception {
        assertMissingPublicNoArgConstructor("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidStatefulBeanPublic.java",
                                            38, "public InvalidStatefulBeanPublic() {\n\t}\n\n\t");
    }

    @Test
    public void testInvalidSingletonBeanPublic() throws Exception {
        assertMissingPublicNoArgConstructor("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidSingletonBeanPublic.java",
                                            39, "public InvalidSingletonBeanPublic() {\n\t}\n\n\t");
    }

    @Test
    public void testValidStatelessBean() throws Exception {
        assertNoDiagnostics("src/main/java/io/openliberty/sample/jakarta/ejb/ValidStatelessBean.java");
    }

    @Test
    public void testValidStatelessBeanNoConstructor() throws Exception {
        assertNoDiagnostics("src/main/java/io/openliberty/sample/jakarta/ejb/ValidStatelessBeanNoConstructor.java");
    }

    @Test
    public void testInvalidStatelessBeanPrivate() throws Exception {
        assertMissingPublicNoArgConstructor("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidStatelessBeanPrivate.java",
                                            40, "public InvalidStatelessBeanPrivate() {\n\t}\n\n\t");
    }

    @Test
    public void testInvalidStatefulBeanPrivate() throws Exception {
        assertMissingPublicNoArgConstructor("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidStatefulBeanPrivate.java",
                                            39, "public InvalidStatefulBeanPrivate() {\n\t}\n\n\t");
    }

    @Test
    public void testInvalidSingletonBeanPrivate() throws Exception {
        assertMissingPublicNoArgConstructor("src/main/java/io/openliberty/sample/jakarta/ejb/InvalidSingletonBeanPrivate.java",
                                            40, "public InvalidSingletonBeanPrivate() {\n\t}\n\n\t");
    }

    private void assertMissingPublicNoArgConstructor(String projectRelativePath, int endCharacter,
                                                     String insertedConstructor) throws Exception {
        String uri = getJavaFileUri(projectRelativePath);
        JakartaJavaDiagnosticsParams diagnosticsParams = createDiagnosticsParams(uri);

        Diagnostic missingConstructorDiagnostic = d(9, 13, endCharacter,
                                                    "Session beans must have a public no-arg constructor.",
                                                    DiagnosticSeverity.Error, "jakarta-ejb", "MissingPublicNoArgConstructor");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, missingConstructorDiagnostic);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, missingConstructorDiagnostic);
        TextEdit insertConstructorEdit = te(11, 4, 11, 4, insertedConstructor);
        CodeAction addConstructorAction = ca(uri, "Add a no-arg public constructor to this class", missingConstructorDiagnostic, insertConstructorEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, addConstructorAction);
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
