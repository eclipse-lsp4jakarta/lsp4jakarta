/*******************************************************************************
* Copyright (c) 2021, 2023 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.RemoveAnnotationProposal;

/**
 * Removes annotations.
 */
public abstract class RemoveAnnotationConflictQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(RemoveAnnotationConflictQuickFix.class.getName());

    /** Annotations to be removed. */
    private final String[] annotations;

    /**
     * Indicator to generate a single action for a list of annotations or one for
     * each annotation in the list.
     */
    protected final boolean generateOnlyOneCodeAction;

    /** Map key to retrieve a list of annotations. */
    public static final String ANNOTATIONS_KEY = "annotations";

    /**
     * Constructor.
     *
     * @param annotations The list of annotations to be removed.
     */
    public RemoveAnnotationConflictQuickFix(String... annotations) {
        this(false, annotations);
    }

    /**
     * Constructor.
     *
     * @param generateOnlyOneCodeAction The single action creation indicator. If
     *            true, a single code action is created to
     *            remove the specified set of annotations;
     *            otherwise, a code action is created per
     *            annotation to delete.
     */
    public RemoveAnnotationConflictQuickFix(boolean generateOnlyOneCodeAction, String... annotations) {
        this.generateOnlyOneCodeAction = generateOnlyOneCodeAction;
        this.annotations = annotations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);
        if (parentType != null) {
            createCodeActions(diagnostic, context, parentType, codeActions);
        }
        return codeActions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);
        CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();
        List<String> annotationToRemoveList = (List<String>) data.getExtendedDataEntry(ANNOTATIONS_KEY);
        String[] annotationToRemove = annotationToRemoveList.toArray(String[]::new);
        String label = getLabel(annotationToRemove);
        ChangeCorrectionProposal proposal = new RemoveAnnotationProposal(label, context.getCompilationUnit(), context.getASTRoot(), parentType, 0, getDeclaringNode(context), annotationToRemove);

        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action to remove annotation", e);
        }

        return toResolve;
    }

    /**
     * Returns the declaring node for the annotation to be removed.
     * Can be overridden by subclasses to provide a different node.
     *
     * @param context The resolve context
     * @return The declaring node
     */
    protected ASTNode getDeclaringNode(JavaCodeActionResolveContext context) {
        return context.getCoveredNode().getParent();
    }

    /**
     * Creates one or more code actions for the given annotations.
     *
     * @param diagnostic The code diagnostic associated with the action to be
     *            created.
     * @param context The context.
     * @param parentType The parent type.
     * @param codeActions The list of code actions.
     *
     * @throws CoreException
     */
    protected void createCodeActions(Diagnostic diagnostic, JavaCodeActionContext context, IBinding parentType,
                                     List<CodeAction> codeActions) throws CoreException {
        if (generateOnlyOneCodeAction) {
            createCodeAction(diagnostic, context, parentType, codeActions, annotations);
        } else {
            for (String annotation : annotations) {
                createCodeAction(diagnostic, context, parentType, codeActions, annotation);
            }
        }
    }

    /**
     * Creates a code action to remove the input annotations.
     *
     * @param diagnostic The code diagnostic associated with the action to be
     *            created.
     * @param context The context.
     * @param parentType The parent type.
     * @param codeActions The list of code actions.
     * @param annotations The annotations to remove.
     *
     *
     * @throws CoreException
     */
    protected void createCodeAction(Diagnostic diagnostic, JavaCodeActionContext context, IBinding parentType,
                                    List<CodeAction> codeActions, String... annotations) throws CoreException {
        String label = getLabel(annotations);

        ExtendedCodeAction codeAction = new ExtendedCodeAction(label);
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        Map<String, Object> extendedData = new HashMap<String, Object>();
        extendedData.put(ANNOTATIONS_KEY, Arrays.asList(annotations));
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

        codeActions.add(codeAction);
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

    protected String[] getAnnotations() {
        return this.annotations;
    }

    /**
     * Returns the label associated with the input annotations.
     *
     * @param annotations The annotations to remove.
     * @return The label associated with the input annotations.
     */
    protected String getLabel(String[] annotations) {
        StringBuilder name = new StringBuilder("Remove ");
        for (int i = 0; i < annotations.length; i++) {
            String annotation = annotations[i];
            String annotationName = annotation.substring(annotation.lastIndexOf('.') + 1, annotation.length());
            if (i > 0) {
                name.append(", "); // assume comma list is ok: @A, @B, @C
            }
            name.append("@"); // Java syntax
            name.append(annotationName);
        }
        return name.toString();
    }

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();
}
