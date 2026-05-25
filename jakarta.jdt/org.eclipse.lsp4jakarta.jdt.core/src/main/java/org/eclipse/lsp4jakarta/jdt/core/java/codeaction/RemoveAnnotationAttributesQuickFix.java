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
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.ASTNodeUtils;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyAnnotationProposal;

/**
 * QuickFix for removing attributes from annotations.
 * Supports both parameter-level and field/method-level annotations.
 */
public abstract class RemoveAnnotationAttributesQuickFix implements IJavaCodeActionParticipant {

    private static final Logger LOGGER = Logger.getLogger(RemoveAnnotationAttributesQuickFix.class.getName());

    /** Map key to retrieve annotation to attributes mapping. */
    protected static final String ANNOTATION_ATTRIBUTES_MAP_KEY = "annotation.attributes.map";

    /** Map key to retrieve parameter names. */
    protected static final String PARAMETER_NAME_KEY = "parameter.name";

    /** Map key to retrieve the annotation name for a specific code action. */
    protected static final String ANNOTATION_KEY = "annotation";

    /** Map of annotation names to their attributes to remove. */
    private final Map<String, List<String>> annotationAttributesMap;

    /** Whether this quick fix operates on method parameters (true) or fields/methods (false). */
    private final boolean isParameterLevel;

    /**
     * Constructor for field/method-level annotations.
     * The map should contain a single entry for field/method-level quick fixes.
     *
     * @param annotationAttributesMap Map of annotation names to their attributes to remove
     * @param isParameterLevel true for parameter-level, false for field/method-level
     */
    public RemoveAnnotationAttributesQuickFix(Map<String, List<String>> annotationAttributesMap, boolean isParameterLevel) {
        this.annotationAttributesMap = annotationAttributesMap;
        this.isParameterLevel = isParameterLevel;
    }

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        return isParameterLevel ? getParameterLevelCodeActions(context, diagnostic) : getFieldMethodLevelCodeActions(context, diagnostic);
    }

    /**
     * Creates code actions for parameter-level annotations.
     */
    @SuppressWarnings("unchecked")
    private List<? extends CodeAction> getParameterLevelCodeActions(JavaCodeActionContext context, Diagnostic diagnostic) {
        List<CodeAction> codeActions = new ArrayList<>();
        ASTNode node = context.getCoveredNode();
        MethodDeclaration parentNode = (MethodDeclaration) node.getParent();
        List<SingleVariableDeclaration> parameters = parentNode.parameters();

        for (SingleVariableDeclaration parameter : parameters) {
            for (Map.Entry<String, List<String>> entry : findAnnotationsWithAttributesToRemove(parameter).entrySet()) {
                createCodeAction(diagnostic, context, codeActions, parameter, entry.getKey(), entry.getValue());
            }
        }
        return codeActions;
    }

    /**
     * Creates code actions for field/method-level annotations.
     */
    private List<? extends CodeAction> getFieldMethodLevelCodeActions(JavaCodeActionContext context, Diagnostic diagnostic) {
        return Collections.singletonList(createCodeAction(getLabel(null, null), diagnostic, context, null));
    }

    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        return isParameterLevel ? resolveParameterLevelCodeAction(context) : resolveFieldMethodLevelCodeAction(context);
    }

    /**
     * Resolves code action for parameter-level annotations.
     */
    @SuppressWarnings("unchecked")
    private CodeAction resolveParameterLevelCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();

        String paramName = (String) data.getExtendedDataEntry(PARAMETER_NAME_KEY);
        String annotation = (String) data.getExtendedDataEntry(ANNOTATION_KEY);
        @SuppressWarnings("unchecked")
        List<String> attributesList = (List<String>) data.getExtendedDataEntry(ANNOTATION_ATTRIBUTES_MAP_KEY);

        MethodDeclaration parentNode = (MethodDeclaration) context.getCoveredNode().getParent();
        IBinding binding = getParameterBinding(parentNode, paramName, annotation);

        if (binding != null) {
            String label = getLabel(annotation, attributesList.toArray(new String[0]));
            ModifyAnnotationProposal proposal = new ModifyAnnotationProposal(label, context.getCompilationUnit(), context.getASTRoot(), binding, 0, annotation, Collections.emptyList(), attributesList);
            setWorkspaceEdit(toResolve, context, proposal);
        }
        return toResolve;
    }

    /**
     * Resolves code action for field/method-level annotations.
     */
    private CodeAction resolveFieldMethodLevelCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        IBinding parentType = getBinding(context.getCoveringNode());
        if (parentType == null) {
            return toResolve;
        }
        String annotationName = annotationAttributesMap.keySet().iterator().next();
        List<String> attributes = annotationAttributesMap.get(annotationName);
        ModifyAnnotationProposal proposal = new ModifyAnnotationProposal(getLabel(null,
                                                                                  null), context.getCompilationUnit(), context.getASTRoot(), parentType, 0, annotationName, Collections.emptyList(), attributes);
        setWorkspaceEdit(toResolve, context, proposal);
        return toResolve;
    }

    /**
     * Sets the workspace edit on the code action from the proposal.
     */
    private void setWorkspaceEdit(CodeAction codeAction, JavaCodeActionResolveContext context, ModifyAnnotationProposal proposal) {
        try {
            codeAction.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action to remove annotation attributes", e);
        }
    }

    /**
     * Gets the binding for field/method-level annotations.
     */
    @SuppressWarnings("restriction")
    protected IBinding getBinding(ASTNode node) {
        ASTNode parent = node.getParent();

        if (parent instanceof MethodDeclaration) {
            return ((MethodDeclaration) parent).resolveBinding();
        }

        if (parent instanceof FieldDeclaration) {
            FieldDeclaration fieldDecl = (FieldDeclaration) parent;
            if (!fieldDecl.fragments().isEmpty()) {
                return ((VariableDeclarationFragment) fieldDecl.fragments().get(0)).resolveBinding();
            }
        }

        return Bindings.getBindingOfParentType(node);
    }

    /**
     * Finds all annotations on a parameter that match the target annotations
     * and have attributes that need to be removed.
     * Returns a map of annotation names to their attributes to remove.
     */
    private Map<String, List<String>> findAnnotationsWithAttributesToRemove(SingleVariableDeclaration parameter) {
        Map<String, List<String>> result = new HashMap<>();
        for (Annotation annotation : getParameterAnnotations(parameter)) {
            ITypeBinding typeBinding = annotation.resolveTypeBinding();
            if (typeBinding != null) {
                String annotationName = typeBinding.getQualifiedName();
                List<String> attributesToRemove = annotationAttributesMap.get(annotationName);
                result.put(annotationName, attributesToRemove);
            }
        }
        return result;
    }

    /**
     * Gets all annotations from a parameter's modifiers.
     */
    @SuppressWarnings("unchecked")
    private List<Annotation> getParameterAnnotations(SingleVariableDeclaration parameter) {
        List<Annotation> result = new ArrayList<>();
        List<ASTNode> modifiers = (List<ASTNode>) parameter.getStructuralProperty(SingleVariableDeclaration.MODIFIERS2_PROPERTY);
        for (ASTNode modifier : modifiers) {
            if (ASTNodeUtils.isAnnotation(modifier)) {
                result.add((Annotation) modifier);
            }
        }
        return result;
    }

    /**
     * Creates a code action with the given parameters.
     */
    private ExtendedCodeAction createCodeAction(String label, Diagnostic diagnostic, JavaCodeActionContext context,
                                                Map<String, Object> extendedData) {
        ExtendedCodeAction codeAction = new ExtendedCodeAction(label);
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));
        return codeAction;
    }

    /**
     * Creates a code action for removing attributes from an annotation on a parameter.
     */
    private void createCodeAction(Diagnostic diagnostic, JavaCodeActionContext context,
                                  List<CodeAction> codeActions, SingleVariableDeclaration parameter,
                                  String annotation, List<String> attributes) {
        String label = getLabel(annotation, attributes.toArray(new String[0]));
        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put(PARAMETER_NAME_KEY, parameter.getName().getIdentifier());
        extendedData.put(ANNOTATION_KEY, annotation);
        extendedData.put(ANNOTATION_ATTRIBUTES_MAP_KEY, attributes);
        codeActions.add(createCodeAction(label, diagnostic, context, extendedData));
    }

    /**
     * Gets the binding for a parameter by name and annotation.
     */
    @SuppressWarnings("unchecked")
    private IBinding getParameterBinding(MethodDeclaration method, String paramName, String targetAnnotation) {
        for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>) method.parameters()) {
            if (param.getName().getIdentifier().equals(paramName)) {
                for (Annotation annotation : getParameterAnnotations(param)) {
                    ITypeBinding typeBinding = annotation.resolveTypeBinding();
                    if (typeBinding != null && typeBinding.getQualifiedName().equals(targetAnnotation)) {
                        return param.resolveBinding();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the label for the code action (for parameter-level).
     * Subclasses must implement this for parameter-level quick fixes.
     *
     * @param annotation The fully qualified annotation name
     * @param attributes The attributes to remove
     * @return The label for the code action
     */
    protected abstract String getLabel(String annotation, String[] attributes);

    protected abstract ICodeActionId getCodeActionId();
}
