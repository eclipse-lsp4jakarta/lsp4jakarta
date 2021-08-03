/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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

package org.jakarta.jdt.servlet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.jakarta.codeAction.IJavaCodeActionParticipant;
import org.jakarta.codeAction.JavaCodeActionContext;
import org.jakarta.codeAction.proposal.ChangeCorrectionProposal;
import org.jakarta.codeAction.proposal.ImplementInterfaceProposal;

/**
 * QuickFix for fixing HttpServlet extension error by providing the code actions
 * which implements IJavaCodeActionParticipant
 *
 * Adapted from
 * https://github.com/eclipse/lsp4mp/blob/master/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/internal/health/java/ImplementHealthCheckQuickFix.java
 *
 * @author Credit to Angelo ZERR
 *
 */
public class ListenerImplementationQuickFix implements IJavaCodeActionParticipant {
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
            IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveredNode();
        ITypeBinding parentType = Bindings.getBindingOfParentType(node);
        if (parentType != null) {
            List<CodeAction> codeActions = new ArrayList<>();
            // Create code action
            // interface

            CodeAction ca0 = setUpCodeAction(parentType, diagnostic, context, ServletConstants.SERVLET_CONTEXT_LISTENER,
                    "jakarta.servlet.ServletContextListener");
            CodeAction ca1 = setUpCodeAction(parentType, diagnostic, context,
                    ServletConstants.SERVLET_CONTEXT_ATTRIBUTE_LISTENER,
                    "jakarta.servlet.ServletContextAttributeListener");
            CodeAction ca2 = setUpCodeAction(parentType, diagnostic, context, ServletConstants.SERVLET_REQUEST_LISTENER,
                    "jakarta.servlet.ServletRequestListener");
            CodeAction ca3 = setUpCodeAction(parentType, diagnostic, context,
                    ServletConstants.SERVLET_REQUEST_ATTRIBUTE_LISTENER,
                    "jakarta.servlet.ServletRequestAttributeListener");
            CodeAction ca4 = setUpCodeAction(parentType, diagnostic, context, ServletConstants.HTTP_SESSION_LISTENER,
                    "jakarta.servlet.http.HttpSessionListener");
            CodeAction ca5 = setUpCodeAction(parentType, diagnostic, context,
                    ServletConstants.HTTP_SESSION_ATTRIBUTE_LISTENER,
                    "jakarta.servlet.http.HttpSessionAttributeListener");
            CodeAction ca6 = setUpCodeAction(parentType, diagnostic, context, ServletConstants.HTTP_SESSION_ID_LISTENER,
                    "jakarta.servlet.http.HttpSessionIdListener");

            codeActions.add(ca0);
            codeActions.add(ca1);
            codeActions.add(ca2);
            codeActions.add(ca3);
            codeActions.add(ca4);
            codeActions.add(ca5);
            codeActions.add(ca6);
            return codeActions;
        }
        return null;
    }

    private CodeAction setUpCodeAction(ITypeBinding parentType, Diagnostic diagnostic, JavaCodeActionContext context,
            String interfaceName, String interfaceType) throws CoreException {
        final String TITLE_MESSAGE = "Let ''{0}'' implement ''{1}''";
        String args[] = { BasicElementLabels.getJavaElementName(parentType.getName()),
                BasicElementLabels.getJavaElementName(interfaceName) };
        ChangeCorrectionProposal proposal = new ImplementInterfaceProposal(MessageFormat.format(TITLE_MESSAGE, args),
                context.getCompilationUnit(), parentType, context.getASTRoot(), interfaceType, 0);
        CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
        codeAction.setTitle(MessageFormat.format(TITLE_MESSAGE, args));

        return codeAction;
    }
}