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
 * Tests for Jakarta EE Enterprise Beans (EJB) @MessageDriven diagnostics.
 *
 * Tests validation that classes annotated with @MessageDriven must implement
 * the appropriate message listener interface (jakarta.jms.MessageListener for JMS).
 *
 * @see <a href="https://jakarta.ee/specifications/enterprise-beans/4.0/jakarta-enterprise-beans-spec-core-4.0#the-required-message-listener-interface">
 *      Jakarta EE Enterprise Beans Specification - Section 5.4.2</a>
 */
public class EjbMessageDrivenTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void messageDrivenBeanValid() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/ejb/MessageDrivenBeanValid.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // No diagnostics expected for valid message-driven bean
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS);
    }

    @Test
    public void messageDrivenBeanMissingInterface() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/ejb/MessageDrivenBeanMissingInterface.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for missing MessageListener interface (on annotation closing line)
        Diagnostic missingInterfaceDiagnostic = d(12, 13, 46,
                                                  "A class annotated with @MessageDriven must implement the jakarta.jms.MessageListener interface.",
                                                  DiagnosticSeverity.Error, "jakarta-ejb", "ImplementMessageListener");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, missingInterfaceDiagnostic);

        // Test code action to implement MessageListener interface
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, missingInterfaceDiagnostic);
        TextEdit textEdit = te(3, 27, 12, 46,
                               "\nimport jakarta.jms.MessageListener;\nimport jakarta.jms.TextMessage;\n\n@MessageDriven(\n    activationConfig = {\n        @jakarta.ejb.ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"jakarta.jms.Queue\"),\n        @jakarta.ejb.ActivationConfigProperty(propertyName = \"destination\", propertyValue = \"jms/MyQueue\")\n    }\n)\npublic class MessageDrivenBeanMissingInterface implements MessageListener");
        CodeAction implementInterfaceAction = ca(uri, "Let 'MessageDrivenBeanMissingInterface' implement 'MessageListener'", missingInterfaceDiagnostic, textEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, implementInterfaceAction);
    }

    @Test
    public void messageDrivenBeanWrongInterface() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(
                                                          new Path("src/main/java/io/openliberty/sample/jakarta/ejb/MessageDrivenBeanWrongInterface.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Test diagnostic for wrong interface (on annotation closing line)
        Diagnostic wrongInterfaceDiagnostic = d(11, 13, 44,
                                                "A class annotated with @MessageDriven must implement the jakarta.jms.MessageListener interface.",
                                                DiagnosticSeverity.Error, "jakarta-ejb", "ImplementMessageListener");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, wrongInterfaceDiagnostic);

        // Test code action to implement MessageListener interface
        JakartaJavaCodeActionParams codeActionParams = createCodeActionParams(uri, wrongInterfaceDiagnostic);
        TextEdit textEdit = te(2, 33, 11, 68,
                               "\nimport jakarta.jms.MessageListener;\n\nimport java.io.Serializable;\n\n@MessageDriven(\n    activationConfig = {\n        @jakarta.ejb.ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"jakarta.jms.Queue\"),\n        @jakarta.ejb.ActivationConfigProperty(propertyName = \"destination\", propertyValue = \"jms/MyQueue\")\n    }\n)\npublic class MessageDrivenBeanWrongInterface implements Serializable, MessageListener");
        CodeAction implementInterfaceAction = ca(uri, "Let 'MessageDrivenBeanWrongInterface' implement 'MessageListener'", wrongInterfaceDiagnostic, textEdit);
        assertJavaCodeAction(codeActionParams, IJDT_UTILS, implementInterfaceAction);
    }
}