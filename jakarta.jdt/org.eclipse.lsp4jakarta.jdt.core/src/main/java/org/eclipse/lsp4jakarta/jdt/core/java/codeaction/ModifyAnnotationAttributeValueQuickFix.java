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
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.ASTNodeUtils;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyAnnotationAttributeValueProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Generic QuickFix for modifying an existing annotation's attribute value.
 * This class allows changing attribute values to qualified names (e.g., enums).
 */
public abstract class ModifyAnnotationAttributeValueQuickFix implements IJavaCodeActionParticipant {

    private static final Logger LOGGER = Logger.getLogger(ModifyAnnotationAttributeValueQuickFix.class.getName());

    protected static final String ANNOTATION_KEY = "annotation";
    protected static final String ATTRIBUTE_NAME_KEY = "attributeName";
    protected static final String NEW_VALUE_KEY = "newValue";

    private final String annotation;
    private final String attributeName;
    private final String newValue;

    /**
     * Constructor for modify annotation attribute value quick fix.
     *
     * @param annotation The fully qualified annotation name (e.g., "jakarta.persistence.Temporal")
     * @param attributeName The attribute name to modify (e.g., "value")
     * @param newValue The new value as a qualified name (e.g., "TemporalType.DATE")
     */
    public ModifyAnnotationAttributeValueQuickFix(String annotation, String attributeName, String newValue) {
        this.annotation = annotation;
        this.attributeName = attributeName;
        this.newValue = newValue;
    }

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        String name = getLabel();
        ExtendedCodeAction codeAction = new ExtendedCodeAction(name);
        codeAction.setRelevance(0);
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        codeAction.setKind(CodeActionKind.QuickFix);

        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put(ANNOTATION_KEY, annotation);
        extendedData.put(ATTRIBUTE_NAME_KEY, attributeName);
        extendedData.put(NEW_VALUE_KEY, newValue);

        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

        codeActions.add(codeAction);
        return codeActions;
    }

    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();
        String resolveAnnotation = (String) data.getExtendedDataEntry(ANNOTATION_KEY);
        String resolveAttributeName = (String) data.getExtendedDataEntry(ATTRIBUTE_NAME_KEY);
        String resolveNewValue = (String) data.getExtendedDataEntry(NEW_VALUE_KEY);

        String name = getLabel();
        ASTNode node = context.getCoveringNode();

        // Walk up the AST to find the annotation node
        Annotation annotationNode = findAnnotationNode(node);
        IBinding parentType;
        if (annotationNode != null) {
            // Get the binding of the field/method that owns the annotation
            parentType = getBinding(annotationNode);
        } else {
            parentType = getBinding(node);
        }

        // Pass the annotation node directly to the proposal
        ChangeCorrectionProposal proposal = new ModifyAnnotationAttributeValueProposal(name, context.getCompilationUnit(), context.getASTRoot(), parentType, annotationNode, 0, resolveAnnotation, resolveAttributeName, resolveNewValue);
        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to create workspace edit for code action to modify annotation attribute value", e);
        }

        return toResolve;
    }

    /**
     * Walks up the AST from the given node to find the enclosing Annotation node.
     *
     * @param node The starting AST node
     * @return The enclosing Annotation node, or null if not found
     */
    protected Annotation findAnnotationNode(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current instanceof Annotation) {
                return (Annotation) current;
            }
            current = current.getParent();
        }
        return null;
    }

    protected IBinding getBinding(ASTNode node) {
        return ASTNodeUtils.getParentTypeBinding(node);
    }

    /**
     * Returns the label for the code action.
     *
     * @return The code action label
     */
    protected String getLabel() {
        String annotationName = annotation.substring(annotation.lastIndexOf('.') + 1);
        return Messages.getMessage("ChangeAttribute", annotationName, attributeName, newValue);
    }

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();
}
