/*******************************************************************************
* Copyright (c) 2021, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.test.servlet;

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
import org.junit.Ignore;
import org.junit.Test;

public class JakartaServletTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void ExtendWebServlet() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/servlet/DontExtendHttpServlet.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // expected
        Diagnostic d = d(5, 13, 34, "Annotated classes with @WebServlet must extend the HttpServlet class.",
                         DiagnosticSeverity.Error, "jakarta-servlet",
                         "WebServletAnnotatedClassDoesNotExtendHttpServlet");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);

        // test associated quick-fix code action
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
        TextEdit te = te(2, 45, 5, 34, "\nimport jakarta.servlet.http.HttpServlet;\n\n"
                                       + "@WebServlet(name = \"demoServlet\", urlPatterns = { \"/demo\" })\npublic class DontExtendHttpServlet extends HttpServlet");

        CodeAction ca = ca(uri, "Let 'DontExtendHttpServlet' extend 'HttpServlet'", d, te);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca);
    }

    @Test
    public void CompleteWebServletAnnotation() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/servlet/InvalidWebServlet.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(9, 0, 13,
                         "The @WebServlet annotation must define the attribute 'urlPatterns' or 'value'.",
                         DiagnosticSeverity.Error, "jakarta-servlet", "WebServletAnnotationMissingAttributes");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
        TextEdit te1 = te(9, 0, 10, 0, "@WebServlet(value = \"\")\n");
        CodeAction ca1 = ca(uri, "Add the `value` attribute to @WebServlet", d, te1);

        TextEdit te2 = te(9, 0, 10, 0, "@WebServlet(urlPatterns = \"\")\n");
        CodeAction ca2 = ca(uri, "Add the `urlPatterns` attribute to @WebServlet", d, te2);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca2, ca1);
    }

    @Test
    public void implementFilter() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/servlet/DontImplementFilter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(5, 13, 32,
                         "Annotated classes with @WebFilter must implement the Filter interface.",
                         DiagnosticSeverity.Error, "jakarta-servlet", "ClassWebFilterAnnotatedNoFilterInterfaceImpl");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
        TextEdit te = te(2, 0, 5, 32, "import jakarta.servlet.Filter;\nimport jakarta.servlet.annotation.WebFilter;\n\n@WebFilter(urlPatterns = "
                                      + "{ \"/filter\" })\npublic class DontImplementFilter implements Filter");
        CodeAction ca = ca(uri, "Let 'DontImplementFilter' implement 'Filter'", d, te);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca);
    }

    @Test
    public void implementListener() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/servlet/DontImplementListener.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(5, 13, 34,
                         "Annotated classes with @WebListener must implement one or more of the following interfaces: ServletContextListener, "
                                    + "ServletContextAttributeListener, ServletRequestListener, ServletRequestAttributeListener, HttpSessionListener, "
                                    + "HttpSessionAttributeListener, or HttpSessionIdListener.",
                         DiagnosticSeverity.Error, "jakarta-servlet", "WebFilterAnnotatedClassReqIfaceNoImpl");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);

        TextEdit te1 = te(2, 46, 5, 34, "\nimport jakarta.servlet.http.HttpSessionAttributeListener;\n\n"
                                        + "@WebListener\npublic class DontImplementListener implements HttpSessionAttributeListener");
        CodeAction ca1 = ca(uri, "Let 'DontImplementListener' implement 'HttpSessionAttributeListener'", d, te1);

        TextEdit te2 = te(2, 46, 5, 34, "\nimport jakarta.servlet.http.HttpSessionIdListener;\n\n"
                                        + "@WebListener\npublic class DontImplementListener implements HttpSessionIdListener");
        CodeAction ca2 = ca(uri, "Let 'DontImplementListener' implement 'HttpSessionIdListener'", d, te2);

        TextEdit te3 = te(2, 46, 5, 34, "\nimport jakarta.servlet.http.HttpSessionListener;\n\n"
                                        + "@WebListener\npublic class DontImplementListener implements HttpSessionListener");
        CodeAction ca3 = ca(uri, "Let 'DontImplementListener' implement 'HttpSessionListener'", d, te3);

        TextEdit te4 = te(2, 0, 5, 34, "import jakarta.servlet.ServletContextAttributeListener;\nimport jakarta.servlet.annotation.WebListener;\n\n"
                                       + "@WebListener\npublic class DontImplementListener implements ServletContextAttributeListener");
        CodeAction ca4 = ca(uri, "Let 'DontImplementListener' implement 'ServletContextAttributeListener'", d, te4);

        TextEdit te5 = te(2, 0, 5, 34, "import jakarta.servlet.ServletContextListener;\nimport jakarta.servlet.annotation.WebListener;\n\n"
                                       + "@WebListener\npublic class DontImplementListener implements ServletContextListener");
        CodeAction ca5 = ca(uri, "Let 'DontImplementListener' implement 'ServletContextListener'", d, te5);

        TextEdit te6 = te(2, 0, 5, 34, "import jakarta.servlet.ServletRequestAttributeListener;\nimport jakarta.servlet.annotation.WebListener;\n\n"
                                       + "@WebListener\npublic class DontImplementListener implements ServletRequestAttributeListener");
        CodeAction ca6 = ca(uri, "Let 'DontImplementListener' implement 'ServletRequestAttributeListener'", d, te6);

        TextEdit te7 = te(2, 0, 5, 34, "import jakarta.servlet.ServletRequestListener;\nimport jakarta.servlet.annotation.WebListener;\n\n"
                                       + "@WebListener\npublic class DontImplementListener implements ServletRequestListener");
        CodeAction ca7 = ca(uri, "Let 'DontImplementListener' implement 'ServletRequestListener'", d, te7);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca1, ca2, ca3, ca4, ca5, ca6, ca7);
    }
    
    
    @Test
    public void RemoveDuplicateAttribute() throws Exception {
        
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/servlet/DuplicateAttributeWebServlet.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));
        
        
        Diagnostic d = d(5, 0, 41,
        		"The @WebServlet annotation cannot have both 'value' and 'urlPatterns' attributes specified at once.",
                DiagnosticSeverity.Error, "jakarta-servlet", "InvalidHttpServletAttribute");


        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
        
        String newText = "package io.openliberty.sample.jakarta.servlet;\n\n" +
                "import jakarta.servlet.annotation.WebServlet;\nimport jakarta.servlet.http.HttpServlet;\n\n@WebServlet( value = \"\")\n" +
                "public class DuplicateAttributeWebServlet extends HttpServlet {\n\n}\n\n";
        String newText1 = "package io.openliberty.sample.jakarta.servlet;\n\nimport jakarta.servlet.annotation.WebServlet;\n" +
                "import jakarta.servlet.http.HttpServlet;\n\n@WebServlet(urlPatterns = \"\" )\n" +
                "public class DuplicateAttributeWebServlet extends HttpServlet {\n\n}\n\n";

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
        
        TextEdit te1 = te(0, 0, 10, 0, newText);
        CodeAction ca1 = ca(uri, "Remove the `urlPatterns` attribute from @WebServlet", d, te1);

        TextEdit te2 = te(0, 0, 10, 0, newText1);
        CodeAction ca2 = ca(uri, "Remove the `value` attribute from @WebServlet", d, te2);

        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca1, ca2);
    
        
    }

    @Test
    public void CompleteWebFilterAnnotation() throws Exception {
    	IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/servlet/InvalidWebFilter.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = d(5, 0, 12,
                "The annotation @WebFilter must define the attribute 'urlPatterns', 'servletNames' or 'value'.",
                DiagnosticSeverity.Error, "jakarta-servlet", "CompleteWebFilterAttributes");
       
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, d);
        
        String newText = "package io.openliberty.sample.jakarta.servlet;\n\nimport jakarta.servlet.*;\n" +
                "import jakarta.servlet.annotation.WebFilter;\n\n@WebFilter(servletNames=\"\")\npublic abstract class InvalidWebFilter " +
                "implements Filter {\n\n}\n\n\n";
        String newText1 = "package io.openliberty.sample.jakarta.servlet;\n\nimport jakarta.servlet.*;\n" +
                "import jakarta.servlet.annotation.WebFilter;\n\n@WebFilter(urlPatterns=\"\")\npublic abstract class InvalidWebFilter " +
                "implements Filter {\n\n}\n\n\n";
        String newText2 = "package io.openliberty.sample.jakarta.servlet;\n\nimport jakarta.servlet.*;\n" +
                "import jakarta.servlet.annotation.WebFilter;\n\n@WebFilter(\"\")\npublic abstract class InvalidWebFilter " +
                "implements Filter {\n\n}\n\n\n";

        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
        
        TextEdit te1 = te(0, 0, 11, 0, newText);
        CodeAction ca1 = ca(uri, "Add the `servletNames` attribute to @WebFilter", d, te1);
        
        TextEdit te2 = te(0, 0, 11, 0, newText1);
        CodeAction ca2 = ca(uri, "Add the `urlPatterns` attribute to @WebFilter", d, te2);
        
        TextEdit te3 = te(0, 0, 11, 0, newText2);
        CodeAction ca3 = ca(uri, "Add the `value` attribute to @WebFilter", d, te3);
        
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, ca1, ca2, ca3);

    }

    @Test
    public void RemoveDuplicateWebFilterAttributes() throws Exception {
        Module module = createMavenModule(new File("src/test/resources/projects/maven/jakarta-sample"));
        IPsiUtils utils = PsiUtilsLSImpl.getInstance(myProject);

        VirtualFile javaFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(ModuleUtilCore.getModuleDirPath(module)
                + "/src/main/java/io/openliberty/sample/jakarta/servlet/DuplicateAttributeWebFilter.java");
        String uri = VfsUtilCore.virtualToIoFile(javaFile).toURI().toString();

        JakartaDiagnosticsParams diagnosticsParams = new JakartaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        Diagnostic d = JakartaForJavaAssert.d(5, 0, 40,
                "The annotation @WebFilter can not have both 'value' and 'urlPatterns' attributes specified at once.",
                DiagnosticSeverity.Error, "jakarta-servlet", "InvalidWebFilterAttribute");

        JakartaForJavaAssert.assertJavaDiagnostics(diagnosticsParams, utils, d);
        String newText1 = "package io.openliberty.sample.jakarta.servlet;\n\nimport jakarta.servlet.annotation.WebFilter;\n" +
                "import jakarta.servlet.Filter;\n\n@WebFilter( value = \"\")\n" +
                "public abstract class DuplicateAttributeWebFilter implements Filter {\n\n}\n\n";
        String newText2 = "package io.openliberty.sample.jakarta.servlet;\n\nimport jakarta.servlet.annotation.WebFilter;\n" +
                "import jakarta.servlet.Filter;\n\n@WebFilter(urlPatterns = \"\" )\n" +
                "public abstract class DuplicateAttributeWebFilter implements Filter {\n\n}\n\n";

        JakartaJavaCodeActionParams codeActionParams = JakartaForJavaAssert.createCodeActionParams(uri, d);

        TextEdit te1 = JakartaForJavaAssert.te(0, 0, 10, 0, newText1);
        CodeAction ca1 = JakartaForJavaAssert.ca(uri, "Remove the `urlPatterns` attribute from @WebFilter", d, te1);

        TextEdit te2 = JakartaForJavaAssert.te(0, 0, 10, 0, newText2);
        CodeAction ca2 = JakartaForJavaAssert.ca(uri, "Remove the `value` attribute from @WebFilter", d, te2);
        JakartaForJavaAssert.assertJavaCodeAction(codeActionParams, utils, ca1, ca2);
    }
}
