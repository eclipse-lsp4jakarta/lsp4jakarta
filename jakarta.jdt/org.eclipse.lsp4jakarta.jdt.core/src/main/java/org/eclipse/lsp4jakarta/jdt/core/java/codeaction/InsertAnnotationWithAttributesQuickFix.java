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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.InsertAnnotationWithAttributesProposal;

/**
 * Generic QuickFix for inserting an annotation with one or more attribute values.
 * This class allows inserting annotations with single or multiple attributes.
 * <p>
 * Examples:
 * <ul>
 * <li>Single attribute: {@code @Temporal(value = TemporalType.DATE)}</li>
 * <li>Multiple attributes: {@code @Column(name = "id", nullable = false)}</li>
 * </ul>
 */
public abstract class InsertAnnotationWithAttributesQuickFix implements IJavaCodeActionParticipant {

    private static final Logger LOGGER = Logger.getLogger(InsertAnnotationWithAttributesQuickFix.class.getName());

    protected static final String ANNOTATION_KEY = "annotation";
    protected static final String ATTRIBUTES_KEY = "attributes";

    private final String annotation;
    private final Map<String, String> attributes;

    /**
     * Constructor for insert annotation with a single attribute quick fix.
     *
     * @param annotation The fully qualified annotation name to insert (e.g., "jakarta.persistence.Temporal")
     * @param attributeName The attribute name (e.g., "value")
     * @param attributeValue The attribute value as a string (e.g., "TemporalType.DATE")
     *            Values should represent the actual Java code (e.g., "\"stringValue\"" for strings,
     *            "42" for numbers, "true" for booleans, "EnumType.VALUE" for enums)
     */
    public InsertAnnotationWithAttributesQuickFix(String annotation, String attributeName, String attributeValue) {
        this.annotation = annotation;
        this.attributes = new LinkedHashMap<>();
        this.attributes.put(attributeName, attributeValue);
    }

    /**
     * Constructor for insert annotation with multiple attributes quick fix.
     *
     * @param annotation The fully qualified annotation name to insert (e.g., "jakarta.persistence.Column")
     * @param attributes Map of attribute names to values (e.g., {"name": "\"id\"", "nullable": "false"})
     *            Values should be strings that represent the actual Java code (e.g., "\"stringValue\"" for strings,
     *            "42" for numbers, "true" for booleans, "EnumType.VALUE" for enums)
     */
    public InsertAnnotationWithAttributesQuickFix(String annotation, Map<String, String> attributes) {
        this.annotation = annotation;
        this.attributes = new LinkedHashMap<>(attributes); // Preserve order
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
        extendedData.put(ATTRIBUTES_KEY, attributes);

        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

        codeActions.add(codeAction);
        return codeActions;
    }

    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();
        String resolveAnnotation = (String) data.getExtendedDataEntry(ANNOTATION_KEY);
        @SuppressWarnings("unchecked")
        Map<String, String> resolveAttributes = (Map<String, String>) data.getExtendedDataEntry(ATTRIBUTES_KEY);

        String name = getLabel();
        ASTNode node = context.getCoveringNode();
        IBinding parentType = getBinding(node);

        // Convert Map<String, String> to Map<String, Object> for the proposal
        Map<String, Object> attributesAsObjects = new LinkedHashMap<>(resolveAttributes);

        ChangeCorrectionProposal proposal = new InsertAnnotationWithAttributesProposal(name, context.getCompilationUnit(), context.getASTRoot(), parentType, 0, resolveAnnotation, attributesAsObjects);
        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to create workspace edit for code action to insert annotation with attributes", e);
        }

        return toResolve;
    }

    protected IBinding getBinding(ASTNode node) {
        // Check if parent is a VariableDeclarationFragment (field)
        if (node.getParent() instanceof VariableDeclarationFragment) {
            return ((VariableDeclarationFragment) node.getParent()).resolveBinding();
        }

        // Walk up the AST to find the method or field declaration
        ASTNode current = node;
        while (current != null) {
            if (current instanceof org.eclipse.jdt.core.dom.MethodDeclaration) {
                return ((org.eclipse.jdt.core.dom.MethodDeclaration) current).resolveBinding();
            } else if (current instanceof org.eclipse.jdt.core.dom.FieldDeclaration) {
                // For field declarations, we need to get the variable binding
                org.eclipse.jdt.core.dom.FieldDeclaration fieldDecl = (org.eclipse.jdt.core.dom.FieldDeclaration) current;
                if (!fieldDecl.fragments().isEmpty()) {
                    return ((VariableDeclarationFragment) fieldDecl.fragments().get(0)).resolveBinding();
                }
            }
            current = current.getParent();
        }

        // Fallback to the original behavior
        return Bindings.getBindingOfParentType(node);
    }

    /**
     * Returns the label for the code action.
     *
     * @return The code action label
     */
    protected String getLabel() {
        String annotationName = annotation.substring(annotation.lastIndexOf('.') + 1);
        StringBuilder label = new StringBuilder("Insert @").append(annotationName).append("(");

        int i = 0;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (i > 0) {
                label.append(", ");
            }
            label.append(entry.getKey()).append(" = ").append(entry.getValue());
            i++;
        }

        label.append(")");
        return label.toString();
    }

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();
}

// Made with Bob