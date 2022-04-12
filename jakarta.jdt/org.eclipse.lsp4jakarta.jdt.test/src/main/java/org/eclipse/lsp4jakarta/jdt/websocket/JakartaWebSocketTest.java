/*******************************************************************************
* Copyright (c) 2022 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.websocket;

import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.core.JakartaForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4jakarta.jdt.core.BaseJakartaTest;
import org.eclipse.lsp4jakarta.jdt.core.JDTUtils;
import org.eclipse.lsp4jakarta.commons.JakartaDiagnosticsParams;
import org.junit.Test;

public class JakartaWebSocketTest extends BaseJakartaTest {
    protected static JDTUtils JDT_UTILS = new JDTUtils();

    @Test
    public void addPathParamsAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject()
                .getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/AnnotationTest.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // OnOpen PathParams Annotation check
        Diagnostic d1 = d(19, 47, 64,
                "Parameters of type String, any Java primitive type, or boxed version thereof must be annotated with @PathParams.",
                DiagnosticSeverity.Error, "jakarta-websocket", "AddPathParamsAnnotation"
        );
        
        // OnClose PathParams Annotation check
        Diagnostic d2 = d(25, 49, 67,
                "Parameters of type String, any Java primitive type, or boxed version thereof must be annotated with @PathParams.",
                DiagnosticSeverity.Error, "jakarta-websocket", "AddPathParamsAnnotation"
        );
        
        Diagnostic d3 = d(25, 76, 94,
                "Parameters of type String, any Java primitive type, or boxed version thereof must be annotated with @PathParams.",
                DiagnosticSeverity.Error, "jakarta-websocket", "AddPathParamsAnnotation"
        );

        Diagnostic d4 = d(31, 48, 65,
                "Parameters of type String, any Java primitive type, or boxed version thereof must be annotated with @PathParams.",
                DiagnosticSeverity.Error, "jakarta-websocket", "AddPathParamsAnnotation"
        );

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d1, d2, d3, d4);        
    }

    @Test
    public void changeInvalidParamType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject()
                .getFile(new Path("src/main/java/io/openliberty/sample/jakarta/websocket/InvalidParamType.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // OnOpen Invalid Param Types
        Diagnostic d1 = d(19, 47, 59,
                "Invalid parameter type. When using @OnOpen, parameter must be of type: \n- jakarta.websocket.EndpointConfig\n- jakarta.websocket.Session\n- annotated with @PathParams and of type String or any Java primitive type or boxed version thereof",
                DiagnosticSeverity.Error, "jakarta-websocket", "OnOpenChangeInvalidParam");
        
        // OnClose Invalid Param Type
        Diagnostic d2 = d(24, 73, 85,
                "Invalid parameter type. When using @OnClose, parameter must be of type: \n- jakarta.websocket.CloseReason\n- jakarta.websocket.Session\n- annotated with @PathParams and of type String or any Java primitive type or boxed version thereof",
                DiagnosticSeverity.Error, "jakarta-websocket", "OnCloseChangeInvalidParam");
        
        // OnError Invalid Param Type
        Diagnostic d3 = d(29, 48, 60,
                "Invalid parameter type. When using @OnError, parameter must be of type: \n- jakarta.websocket.Session\n- java.lang.Throwable\n- annotated with @PathParams and of type String or any Java primitive type or boxed version thereof",
                DiagnosticSeverity.Error, "jakarta-websocket", "OnErrorChangeInvalidParam");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d1, d2, d3);
    }
    
    @Test
    public void testPathParamInvalidURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                new Path("src/main/java/io/openliberty/sample/jakarta/websockets/PathParamURIWarningTest.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(22, 59, 77, "PathParam value does not match specified Endpoint URI",
                DiagnosticSeverity.Warning, "jakarta-websocket", "ChangePathParamValue");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d);
    }

    @Test
    public void testInvalidDupParams() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                new Path("src/main/java/io/openliberty/sample/jakarta/websocket/DuplicateParamCheck.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        String message = "Duplicate parameter types not allowed. @OnError requires to include only one parameter of type: \n- jakarta.websocket.Session\n- java.lang.Throwable\n";

        Diagnostic d1 = d(15, 69, 75, message, DiagnosticSeverity.Error,
                "jakarta-websocket", "DuplicateParamsTypes");

        Diagnostic d2 = d(15, 51, 57, message, DiagnosticSeverity.Error,
                "jakarta-websocket", "DuplicateParamsTypes");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d1, d2);
    }

    @Test
    public void testMandParamsType() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                new Path("src/main/java/io/openliberty/sample/jakarta/websocket/MandatoryParamTypes.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        String message = "Mandatory parameters missing. @OnError requires to include mandatory parameters of type: \n- java.lang.Throwable\n";

        Diagnostic d = d(13, 16, 23, message, DiagnosticSeverity.Error,
                "jakarta-websocket", "OnErrorMandatoryParamMissing");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d);
    }

    @Test
    public void testServerEndpointRelativeURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointRelativePathTest.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d = d(6, 0, 27,
                "Server endpoint paths must not contain the sequences '/../', '/./' or '//'.",
                DiagnosticSeverity.Error, "jakarta-websocket", "ChangeInvalidServerEndpoint");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d);
    }

    @Test
    public void testServerEndpointNoSlashURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointNoSlash.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d1 = d(7, 0, 23,
                "Server endpoint paths must start with a leading '/'.",
                DiagnosticSeverity.Error, "jakarta-websocket", "ChangeInvalidServerEndpoint");
        Diagnostic d2 = d(7, 0, 23,
                "Server endpoint paths must be a URI-template (level-1) or a partial URI.",
                DiagnosticSeverity.Error, "jakarta-websocket", "ChangeInvalidServerEndpoint");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d1, d2);
    }

    @Test
    public void testServerEndpointInvalidTemplateURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointInvalidTemplateURI.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d = d(6, 0, 46,
                "Server endpoint paths must be a URI-template (level-1) or a partial URI.",
                DiagnosticSeverity.Error, "jakarta-websocket", "ChangeInvalidServerEndpoint");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d);
    }

    @Test
    public void testServerEndpointDuplicateVariableURI() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                new Path("src/main/java/io/openliberty/sample/jakarta/websocket/ServerEndpointDuplicateVariableURI.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        Diagnostic d = d(6, 0, 40,
                "Server endpoint paths must not use the same variable more than once in a path.",
                DiagnosticSeverity.Error, "jakarta-websocket", "ChangeInvalidServerEndpoint");

        assertJavaDiagnostics(diagnosticsParams, JDT_UTILS, d);
    }
}