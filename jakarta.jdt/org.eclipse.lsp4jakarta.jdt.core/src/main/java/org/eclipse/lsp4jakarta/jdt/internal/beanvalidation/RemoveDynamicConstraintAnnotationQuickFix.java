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
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.beanvalidation;

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
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.RemoveAnnotationProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Removes bean validation constraints (@AssertTrue, @NotNull, @Digits, @Size,
 * etc.) provided by the diagnostic data.
 */
public class RemoveDynamicConstraintAnnotationQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(RemoveDynamicConstraintAnnotationQuickFix.class.getName());

    /** Annotation name map key */
    public static final String ANNOTATION_NAME_KEY = "annotation.name";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveDynamicConstraintAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        String annotationName = diagnostic.getData().toString().replace("\"", "");
        String label = getLabel(annotationName);
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);
        List<CodeAction> codeActions = new ArrayList<>();
        if (parentType != null) {
            ExtendedCodeAction codeAction = new ExtendedCodeAction(label);
            codeAction.setRelevance(0);
            codeAction.setKind(CodeActionKind.QuickFix);
            codeAction.setDiagnostics(Arrays.asList(diagnostic));
            Map<String, Object> extendedData = new HashMap<String, Object>();
            extendedData.put(ANNOTATION_NAME_KEY, annotationName);

            codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), JakartaCodeActionId.RemoveConstraintAnnotation));
            codeActions.add(codeAction);
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
        String annotationName = (String) data.getExtendedDataEntry(ANNOTATION_NAME_KEY);
        String label = getLabel(annotationName);
        ChangeCorrectionProposal proposal = new RemoveAnnotationProposal(label, context.getCompilationUnit(), context.getASTRoot(), parentType, 0, context.getCoveredNode().getParent(), annotationName);

        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action to remove a constraint annotation",
                       e);
        }

        return toResolve;
    }

    /**
     * Returns the code action label.
     *
     * @Paran annotationName The annotation name.
     *
     * @return The code action label.
     */
    private static String getLabel(String annotation) {
        String[] parts = annotation.split("\\.");
        String annotationName = (parts.length > 1) ? parts[parts.length - 1] : annotation;
        return Messages.getMessage("RemoveConstraintAnnotation", annotationName);
    }

    /**
     * Returns the named entity associated to the given node.
     *
     * @param node The AST Node
     *
     * @return The named entity associated to the given node.
     */
    @SuppressWarnings("restriction")
    protected static IBinding getBinding(ASTNode node) {
        if (node.getParent() instanceof VariableDeclarationFragment) {
            return ((VariableDeclarationFragment) node.getParent()).resolveBinding();
        }

        return org.eclipse.jdt.internal.corext.dom.Bindings.getBindingOfParentType(node);
    }
}
