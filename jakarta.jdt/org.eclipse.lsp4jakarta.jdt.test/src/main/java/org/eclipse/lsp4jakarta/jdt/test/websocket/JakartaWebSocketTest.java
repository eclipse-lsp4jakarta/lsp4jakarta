/*******************************************************************************
* Copyright (c) 2022, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.test.websocket;

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

public class JakartaWebSocketTest extends BaseJakartaTest {
    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void addPathParamsAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/AnnotationTest.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // OnOpen PathParams Annotation check
        Diagnostic d1 = d(18, 47, 64,
                          "Parameters of type String, any Java primitive type, or boxed version thereof must be annotated with @PathParams.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "PathParamsMissingFromParam");

        // OnClose PathParams Annotation check
        Diagnostic d2 = d(24, 49, 67,
                          "Parameters of type String, any Java primitive type, or boxed version thereof must be annotated with @PathParams.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "PathParamsMissingFromParam");

        Diagnostic d3 = d(24, 76, 94,
                          "Parameters of type String, any Java primitive type, or boxed version thereof must be annotated with @PathParams.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "PathParamsMissingFromParam");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2, d3);

        // Expected code actions
        JakartaJavaCodeActionParams codeActionsParams = createCodeActionParams(uri, d1);
        String newText = "\nimport jakarta.websocket.server.PathParam;\nimport jakarta.websocket.server.ServerEndpoint;\nimport jakarta.websocket.Session;\n\n"
                         + "/**\n * Expected Diagnostics are related to validating that the parameters have the \n * valid annotation @PathParam (code: AddPathParamsAnnotation)\n * See issue #247 (onOpen) and #248 (onClose)\n */\n"
                         + "@ServerEndpoint(value = \"/infos\")\npublic class AnnotationTest {\n    // @PathParam missing annotation for \"String missingAnnotation\"\n    @OnOpen\n    public void OnOpen(Session session, @PathParam(value = \"\") ";
        TextEdit te = te(5, 32, 18, 40, newText);
        CodeAction ca = ca(uri, "Insert @PathParam", d1, te);
        assertJavaCodeAction(codeActionsParams, IJDT_UTILS, ca);
    }

    @Test
    public void changeInvalidParamType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/InvalidParamType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // OnOpen Invalid Param Types
        Diagnostic d1 = d(19, 47, 59,
                          "Invalid parameter type. When using @OnOpen, parameter must be of type: \n- jakarta.websocket.EndpointConfig\n- jakarta.websocket.Session\n- annotated with @PathParams and of type String or any Java primitive type or boxed version thereof.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "InvalidOnOpenParams");

        // OnClose Invalid Param Type
        Diagnostic d2 = d(24, 73, 85,
                          "Invalid parameter type. When using @OnClose, parameter must be of type: \n- jakarta.websocket.CloseReason\n- jakarta.websocket.Session\n- annotated with @PathParams and of type String or any Java primitive type or boxed version thereof.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "InvalidOnCloseParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);
    }

    @Test
    public void testPathParamInvalidURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/websockets/PathParamURIWarningTest.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(22, 59, 77, "PathParam value does not match specified Endpoint URI.",
                         DiagnosticSeverity.Warning, "jakarta-websocket", "PathParamDoesNotMatchEndpointURI");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

    @Test
    public void testServerEndpointRelativeURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointRelativePathTest.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(6, 0, 27, "Server endpoint paths must not contain the sequences '/../', '/./' or '//'.",
                         DiagnosticSeverity.Error, "jakarta-websocket", "InvalidEndpointPathWithRelativePaths");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

    @Test
    public void testServerEndpointNoSlashURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointNoSlash.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d1 = d(7, 0, 23, "Server endpoint paths must start with a leading '/'.", DiagnosticSeverity.Error,
                          "jakarta-websocket", "InvalidEndpointPathWithNoStartingSlash");
        Diagnostic d2 = d(7, 0, 23, "Server endpoint paths must be a URI-template (level-1) or a partial URI.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "InvalidEndpointPathNotTempleateOrPartialURI");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        // Expected code actions
        JakartaJavaCodeActionParams codeActionsParams = createCodeActionParams(uri, d1);
        String newText = "\"/path\"";
        TextEdit te = te(7, 16, 7, 22, newText);
        CodeAction ca = ca(uri, "Prefix value with '/'", d1, te);
        assertJavaCodeAction(codeActionsParams, IJDT_UTILS, ca);

    }

    @Test
    public void testServerEndpointNoSlashWithValueAttribute() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointNoSlashValueAttribte.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d1 = d(7, 0, 29, "Server endpoint paths must start with a leading '/'.", DiagnosticSeverity.Error,
                          "jakarta-websocket", "InvalidEndpointPathWithNoStartingSlash");
        Diagnostic d2 = d(7, 0, 29, "Server endpoint paths must be a URI-template (level-1) or a partial URI.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "InvalidEndpointPathNotTempleateOrPartialURI");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);

        // Expected code actions
        JakartaJavaCodeActionParams codeActionsParams = createCodeActionParams(uri, d1);
        String newText = "\"/path\"";
        TextEdit te = te(7, 22, 7, 28, newText);
        CodeAction ca = ca(uri, "Prefix value with '/'", d1, te);
        assertJavaCodeAction(codeActionsParams, IJDT_UTILS, ca);

    }

    @Test
    public void testServerEndpointInvalidTemplateURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointInvalidTemplateURI.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d = d(6, 0, 46, "Server endpoint paths must be a URI-template (level-1) or a partial URI.",
                         DiagnosticSeverity.Error, "jakarta-websocket", "InvalidEndpointPathNotTempleateOrPartialURI");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

    @Test
    public void testServerEndpointDuplicateVariableURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointDuplicateVariableURI.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d = d(6, 0, 40, "Server endpoint paths must not use the same variable more than once in a path.",
                         DiagnosticSeverity.Error, "jakarta-websocket", "InvalidEndpointPathDuplicateVariable");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
    }

    public void testDuplicateOnMessage() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/DuplicateOnMessage.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d1 = d(11, 4, 14,
                          "Classes annotated with @ServerEndpoint or @ClientEndpoint may only have one @OnMessage annotated method for each of the native WebSocket message formats: text, binary and pong.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "OnMessageDuplicateMethod");
        Diagnostic d2 = d(16, 4, 14,
                          "Classes annotated with @ServerEndpoint or @ClientEndpoint may only have one @OnMessage annotated method for each of the native WebSocket message formats: text, binary and pong.",
                          DiagnosticSeverity.Error, "jakarta-websocket", "OnMessageDuplicateMethod");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d1, d2);
    }
}
