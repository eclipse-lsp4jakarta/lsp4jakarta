/*******************************************************************************
* Copyright (c) 2021, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.InsertAnnotationAttributeProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * QuickFix for inserting attribute of a given annotation.
 *
 * @author Angelo ZERR
 *
 */
public abstract class InsertAnnotationAttributeQuickFix implements IJavaCodeActionParticipant {

    private static final Logger LOGGER = Logger.getLogger(InsertAnnotationAttributeQuickFix.class.getName());

    private final String attributeName;

    /**
     * Constructor for inserting attribute annotation quick fix.
     *
     * @param attribute name list of annotation to insert.
     */
    public InsertAnnotationAttributeQuickFix(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel(attributeName));
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));
        return Collections.singletonList(codeAction);
    }

    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode selectedNode = context.getCoveringNode();
        Annotation annotation = (Annotation) selectedNode.getParent().getParent();
        String name = getLabel(attributeName);
        ChangeCorrectionProposal proposal = new InsertAnnotationAttributeProposal(name, context.getCompilationUnit(), annotation, 0, attributeName);
        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action edit for inserting an attribute value", e);
        }
        return toResolve;
    }

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();

    private static String getLabel(String memberName) {
        return Messages.getMessage("InsertAttribute", memberName);
    }

}
