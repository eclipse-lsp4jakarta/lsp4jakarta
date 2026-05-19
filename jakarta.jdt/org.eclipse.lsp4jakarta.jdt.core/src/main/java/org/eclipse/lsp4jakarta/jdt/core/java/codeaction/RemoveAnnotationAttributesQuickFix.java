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
import java.util.Arrays;
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
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
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

    /** Map key to retrieve a list of annotations. */
    protected static final String ANNOTATIONS_KEY = "annotations";

    /** Map key to retrieve parameter names. */
    protected static final String PARAMETER_NAME_KEY = "parameter.name";

    /** Map key to retrieve attributes to remove. */
    protected static final String ATTRIBUTES_KEY = "attributes";

    /** Annotations to check (can be single or multiple). */
    private final String[] annotations;

    /** Attributes to remove. */
    private final List<String> attributeNames;

    /** Whether this quick fix operates on method parameters (true) or fields/methods (false). */
    private final boolean isParameterLevel;

    /**
     * Constructor for field/method-level annotations (single annotation).
     *
     * @param annotationName The fully qualified annotation name
     * @param attributeNames The list of attribute names to remove
     */
    public RemoveAnnotationAttributesQuickFix(String annotationName, List<String> attributeNames) {
        this.annotations = new String[] { annotationName };
        this.attributeNames = attributeNames;
        this.isParameterLevel = false;
    }

    /**
     * Constructor for parameter-level annotations (can be multiple annotations).
     *
     * @param annotations Array of fully qualified annotation names
     * @param attributes Varargs of attribute names to remove
     */
    public RemoveAnnotationAttributesQuickFix(String[] annotations, String... attributes) {
        this.annotations = annotations;
        this.attributeNames = Arrays.asList(attributes);
        this.isParameterLevel = true;
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
            for (String annotation : findAnnotationsWithAttributesToRemove(parameter)) {
                createCodeAction(diagnostic, context, codeActions, parameter, annotation);
            }
        }
        return codeActions;
    }

    /**
     * Creates code actions for field/method-level annotations.
     */
    private List<? extends CodeAction> getFieldMethodLevelCodeActions(JavaCodeActionContext context, Diagnostic diagnostic) {
        return Collections.singletonList(createCodeAction(getLabel(), diagnostic, context, null));
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
        List<String> annotationsList = (List<String>) data.getExtendedDataEntry(ANNOTATIONS_KEY);
        List<String> attributesList = (List<String>) data.getExtendedDataEntry(ATTRIBUTES_KEY);

        MethodDeclaration parentNode = (MethodDeclaration) context.getCoveredNode().getParent();
        IBinding binding = getParameterBinding(parentNode, paramName, annotationsList.toArray(new String[0]));

        if (binding != null) {
            String label = getLabel(annotationsList.get(0), attributesList.toArray(new String[0]));
            ModifyAnnotationProposal proposal = new ModifyAnnotationProposal(label, context.getCompilationUnit(), context.getASTRoot(), binding, 0, annotationsList.get(0), Collections.emptyList(), attributesList);
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
        ModifyAnnotationProposal proposal = new ModifyAnnotationProposal(getLabel(), context.getCompilationUnit(), context.getASTRoot(), parentType, 0, annotations[0], Collections.emptyList(), attributeNames);
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
     */
    private List<String> findAnnotationsWithAttributesToRemove(SingleVariableDeclaration parameter) {
        List<String> result = new ArrayList<>();
        for (Annotation annotation : getParameterAnnotations(parameter)) {
            ITypeBinding typeBinding = annotation.resolveTypeBinding();
            if (typeBinding != null && isTargetAnnotation(typeBinding.getQualifiedName())
                && hasAttributesToRemove(annotation)) {
                result.add(typeBinding.getQualifiedName());
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
     * Checks if the given annotation name matches any of the target annotations.
     */
    private boolean isTargetAnnotation(String annotationName) {
        for (String annotation : annotations) {
            if (annotation.equals(annotationName)) {
                return true;
            }
        }
        return false;
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
                                  String annotation) {
        String label = getLabel(annotation, attributeNames.toArray(new String[0]));
        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put(PARAMETER_NAME_KEY, parameter.getName().getIdentifier());
        extendedData.put(ANNOTATIONS_KEY, Collections.singletonList(annotation));
        extendedData.put(ATTRIBUTES_KEY, attributeNames);
        codeActions.add(createCodeAction(label, diagnostic, context, extendedData));
    }

    /**
     * Gets the binding for a parameter by name and annotation.
     */
    @SuppressWarnings("unchecked")
    private IBinding getParameterBinding(MethodDeclaration method, String paramName, String[] targetAnnotations) {
        for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>) method.parameters()) {
            if (param.getName().getIdentifier().equals(paramName)) {
                for (Annotation annotation : getParameterAnnotations(param)) {
                    ITypeBinding typeBinding = annotation.resolveTypeBinding();
                    if (typeBinding != null && isTargetAnnotation(typeBinding.getQualifiedName())) {
                        return param.resolveBinding();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if the given annotation has any of the attributes that need to be removed.
     * This prevents code actions from appearing after attributes have already been removed.
     * Subclasses can override shouldRemoveAttribute() to add value-based checks.
     */
    @SuppressWarnings("unchecked")
    protected boolean hasAttributesToRemove(Annotation annotation) {
        if (!(annotation instanceof NormalAnnotation)) {
            return false;
        }

        List<MemberValuePair> values = ((NormalAnnotation) annotation).values();
        for (String attributeToRemove : attributeNames) {
            for (MemberValuePair mvp : values) {
                if (mvp.getName().getFullyQualifiedName().equals(attributeToRemove)
                    && shouldRemoveAttribute(mvp)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a specific attribute should be removed based on its value.
     * Default: remove regardless of value. Override to add value checks.
     *
     * @param memberValuePair The member value pair to check
     * @return true if the attribute should be removed
     */
    protected boolean shouldRemoveAttribute(MemberValuePair memberValuePair) {
        return true;
    }

    /**
     * Returns the label for the code action (for field/method-level).
     * Subclasses must implement this for field/method-level quick fixes.
     *
     * @return The label for the code action
     */
    protected String getLabel() {
        return getLabel(annotations[0], attributeNames.toArray(new String[0]));
    }

    /**
     * Returns the label for the code action (for parameter-level).
     * Subclasses must implement this for parameter-level quick fixes.
     *
     * @param annotation The fully qualified annotation name
     * @param attributes The attributes to remove
     * @return The label for the code action
     */
    protected String getLabel(String annotation, String[] attributes) {
        return getLabel();
    }

    protected abstract ICodeActionId getCodeActionId();
}
