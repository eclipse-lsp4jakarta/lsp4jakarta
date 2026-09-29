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
 * Tests for CDI scope validation on @Stateless session beans.
 * Test resources are under: io/openliberty/sample/jakarta/cdi/sessionbean/
 *
 * All files share this structure (0-based lines):
 * line 6: @Stateless
 * line 7: @XxxScoped (first scope annotation, if any)
 * line 8: @YyyScoped (second scope, for 3-annotation files)
 * line 8 or 10: public class ClassName {
 *
 * Class name always starts at col 13 ("public class " = 13 chars).
 */
public class StatelessSessionBeanTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    // -----------------------------------------------------------------------
    // StatelessSessionBean.java — @Stateless + @RequestScoped (invalid)
    // 3 imports → @Stateless on line 7, @RequestScoped on line 8, class decl line 9
    // "public class StatelessSessionBean {" → col 13..33
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessWithRequestScope() throws Exception {
        String uri = getFileUri("StatelessSessionBean.java");

        Diagnostic invalidScope = d(9, 13, 33,
                                    "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, invalidScope);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, invalidScope);
        TextEdit removeStateless = te(7, 0, 8, 0, "");
        TextEdit replaceWithDependent = te(7, 0, 9, 0, "@Dependent\n@Stateless\n");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Stateless", invalidScope, removeStateless),
                             ca(uri, "Replace current scope with @Dependent", invalidScope, replaceWithDependent));
    }

    // -----------------------------------------------------------------------
    // StatelessWithSessionScoped.java — @Stateless + @SessionScoped (invalid)
    // 3 imports → @Stateless on line 7, @SessionScoped on line 8, class decl line 9
    // "public class StatelessWithSessionScoped {" → col 13..39
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessWithSessionScope() throws Exception {
        String uri = getFileUri("StatelessWithSessionScoped.java");

        Diagnostic invalidScope = d(9, 13, 39,
                                    "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");
        Diagnostic missingSerializable = d(9, 13, 39,
                                           "A managed bean in a passivating scope must implement java.io.Serializable.",
                                           DiagnosticSeverity.Error, "jakarta-cdi", "InvalidPassivatingScopedBeanWithoutSerializable");

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, invalidScope, missingSerializable);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, invalidScope);
        TextEdit removeStateless = te(7, 0, 8, 0, "");
        TextEdit replaceWithDependent = te(7, 0, 9, 0, "@Dependent\n@Stateless\n");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Stateless", invalidScope, removeStateless),
                             ca(uri, "Replace current scope with @Dependent", invalidScope, replaceWithDependent));
    }

    // -----------------------------------------------------------------------
    // StatelessWithMultipleScopes.java — @Stateless + @Dependent + @RequestScoped (invalid)
    // class decl line 10, "public class StatelessWithMultipleScopes {" → col 13..40
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessWithMultipleScopes() throws Exception {
        String uri = getFileUri("StatelessWithMultipleScopes.java");

        Diagnostic invalidScope = d(10, 13, 40,
                                    "A stateless session bean belongs to the @Dependent scope. Any other scope is invalid.",
                                    DiagnosticSeverity.Error, "jakarta-cdi", "InvalidStatelessSessionBeanScope");

        assertJavaDiagnostics(createDiagnosticsParams(uri), IJDT_UTILS, invalidScope);

        JakartaJavaCodeActionParams params = createCodeActionParams(uri, invalidScope);
        TextEdit removeStateless = te(7, 0, 8, 0, "");
        TextEdit replaceWithDependent = te(7, 0, 10, 0, "@Dependent\n@Stateless\n");
        assertJavaCodeAction(params, IJDT_UTILS,
                             ca(uri, "Remove @Stateless", invalidScope, removeStateless),
                             ca(uri, "Replace current scope with @Dependent", invalidScope, replaceWithDependent));
    }

    // -----------------------------------------------------------------------
    // StatelessWithNoScope.java — @Stateless only (valid)
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessWithNoScopeIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("StatelessWithNoScope.java")), IJDT_UTILS);
    }

    // -----------------------------------------------------------------------
    // StatelessWithDependent.java — @Stateless + @Dependent (valid)
    // -----------------------------------------------------------------------
    @Test
    public void testStatelessWithDependentIsValid() throws Exception {
        assertJavaDiagnostics(createDiagnosticsParams(getFileUri("StatelessWithDependent.java")), IJDT_UTILS);
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
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/cdi/sessionbean/" + fileName));
        return javaFile.getLocation().toFile().toURI().toString();
    }
}
