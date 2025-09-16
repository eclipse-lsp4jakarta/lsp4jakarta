/*******************************************************************************
* Copyright (c) 2023, 2025 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyAnnotationProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Inserts the specified set of attributes the the specified annotation.
 */
public abstract class InsertAnnotationAttributesQuickFix implements IJavaCodeActionParticipant {
    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(InsertAnnotationAttributesQuickFix.class.getName());

    /** The annotation to which attributes are added. */
    private final String annotation;

    /** The attributes the add to the annotation. */
    private final String[] attributes;

    /**
     * Constructor.
     *
     * @param annotation The fully qualified annotation to modify.
     * @param attributes The attribute names to add to given annotation.
     */
    public InsertAnnotationAttributesQuickFix(String annotation, String... attributes) {
        this.annotation = annotation;
        this.attributes = attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertAnnotationAttributesQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        String name = getLabel(annotation, attributes);
        ExtendedCodeAction codeAction = new ExtendedCodeAction(name);
        codeAction.setRelevance(0);
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        codeAction.setKind(CodeActionKind.QuickFix);

        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));
        codeActions.add(codeAction);

        return codeActions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        String label = getLabel(annotation, attributes);
        ASTNode node = context.getCoveringNode();
        IBinding parentType = getBinding(node);
        ChangeCorrectionProposal proposal = new ModifyAnnotationProposal(label, context.getCompilationUnit(), context.getASTRoot(), parentType, 0, Arrays.asList(attributes), annotation);

        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE,
                       "Unable to resolve code action edit for inserting an anotation with attributes",
                       e);
        }

        return toResolve;
    }

    /**
     * Returns the id for this code action.
     *
     * @return The id for this code action.
     */
    protected abstract ICodeActionId getCodeActionId();

    /**
     * Returns the code action label.
     *
     * @param annotaiton The annotation name.
     * @param attributes The attribute names.
     *
     * @return The code action label.
     */
    protected String getLabel(String annotation, String[] attributes) {
        String[] parts = annotation.split("\\.");
        String AnnotationName = (parts.length > 1) ? parts[parts.length - 1] : annotation;
        String atributeNames = String.join(",", attributes);
        String pluralSuffix = (attributes.length > 1) ? "s" : "";
        return Messages.getMessage("InsertAttributes", atributeNames, pluralSuffix, AnnotationName);
    }

    /**
     * Returns the named entity associated to the given node.
     *
     * @param node The AST Node
     *
     * @return The named entity associated to the given node.
     */
    @SuppressWarnings("restriction")
    protected IBinding getBinding(ASTNode node) {
        if (node.getParent() instanceof VariableDeclarationFragment) {
            return ((VariableDeclarationFragment) node.getParent()).resolveBinding();
        }
        return org.eclipse.jdt.internal.corext.dom.Bindings.getBindingOfParentType(node);
    }
}