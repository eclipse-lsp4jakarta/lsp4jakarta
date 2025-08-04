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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyModifiersProposal;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Removes any of @Disposes, @Observes and @ObservesAsync
 * annotation from the declaring element.
 */
public abstract class RemoveMethodParamAnnotationQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(RemoveMethodParamAnnotationQuickFix.class.getName());

    /** Map key to retrieve a list of annotations. */
    protected static final String ANNOTATIONS_KEY = "annotations";

    /** Map key to retrieve parameter names. */
    protected static final String PARAMETER_NAME_KEY = "parameter.name";

    /** Annotations to remove. */
    String[] annotations;

    public RemoveMethodParamAnnotationQuickFix(String... annotations) {
        this.annotations = annotations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveMethodParamAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();

        ASTNode node = context.getCoveredNode();
        MethodDeclaration parentNode = (MethodDeclaration) node.getParent();
        List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) parentNode.parameters();

        for (SingleVariableDeclaration parameter : parameters) {

            List<ASTNode> modifiers = (List<ASTNode>) parameter.getStructuralProperty(SingleVariableDeclaration.MODIFIERS2_PROPERTY);
            ArrayList<String> annotationsToRemove = new ArrayList<>();

            for (ASTNode modifier : modifiers) {
                if (modifier instanceof MarkerAnnotation) {
                    MarkerAnnotation annotation = (MarkerAnnotation) modifier;
                    ITypeBinding typeBinding = annotation.resolveTypeBinding();
                    if (Arrays.asList(this.annotations).stream().anyMatch(m -> m.equals(typeBinding.getQualifiedName()))) {
                        annotationsToRemove.add(annotation.getTypeName().toString());
                    }
                }
            }

            // in the case of a method sig:
            // public String greetDisposesObservesObservesAsync(@Disposes String name1, String name2, @ObservesAsync String name3)
            // parameter name2 will have no annotation to remove - but this is still in need of a QF because params 1 & 3 are in conflict
            // when processing param 2 we need to account for the fact that no QF is needed for that particular param, but
            // a QF is needed for param 3. Previously the fact that param2 had no annotation to remove caused an ArrayIndexOOB when creating the label (QF string)
            if (annotationsToRemove.size() > 0) {
                createCodeAction(diagnostic, context, codeActions, parameter,
                                 (String[]) annotationsToRemove.toArray(new String[annotationsToRemove.size()]));
            }
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
        MethodDeclaration parentNode = (MethodDeclaration) node.getParent();
        IMethodBinding parentMethod = parentNode.resolveBinding();
        CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();
        List<String> annotationsToRemoveList = (List<String>) data.getExtendedDataEntry(ANNOTATIONS_KEY);
        String[] annotationsToRemove = annotationsToRemoveList.toArray(String[]::new);
        String parameterName = (String) data.getExtendedDataEntry(PARAMETER_NAME_KEY);

        SingleVariableDeclaration parameter = matchParameterBinding(parentNode, parameterName);
        if (parameter != null) {
            String label = getLabel(parameterName, annotationsToRemove);
            ModifyModifiersProposal proposal = new ModifyModifiersProposal(label, context.getCompilationUnit(), context.getASTRoot(), parentMethod, 0, parameter, new ArrayList<>(), Arrays.asList(annotationsToRemove));

            try {
                toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
            } catch (CoreException e) {
                LOGGER.log(Level.SEVERE, "Unable to resolve code action to remove annotation", e);
            }
        }
        return toResolve;
    }

    /**
     *
     * The code diagnostic associated with the action to be
     * created.
     *
     * @param context The context.
     * @param codeActions The list of code action to update.
     * @param parameter The method parameter for which to create a code
     *            action.
     * @param annotationsToRemove The annotations to remove.
     *
     * @throws CoreException
     */
    protected void createCodeAction(Diagnostic diagnostic, JavaCodeActionContext context,
                                    List<CodeAction> codeActions, SingleVariableDeclaration parameter, String... annotationsToRemove) throws CoreException {
        String parameterName = parameter.getName().toString();
        String label = getLabel(parameterName, annotationsToRemove);
        ExtendedCodeAction codeAction = new ExtendedCodeAction(label);
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        Map<String, Object> extendedData = new HashMap<String, Object>();
        extendedData.put(ANNOTATIONS_KEY, Arrays.asList(annotationsToRemove));
        extendedData.put(PARAMETER_NAME_KEY, parameterName);

        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

        codeActions.add(codeAction);
    }

    /**
     * Returns the object representing the method parameter associated with the
     * input parameter name.
     *
     * @param method The method node.
     * @param parameterName The parameter name.
     * @return The object representing the method parameter associated with the
     *         input parameter name.
     */
    private SingleVariableDeclaration matchParameterBinding(MethodDeclaration method, String parameterName) {

        List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) method.parameters();

        for (SingleVariableDeclaration parameter : parameters) {
            if (parameter.getName().toString().equals(parameterName)) {
                return parameter;
            }
        }

        return null;
    }

    /**
     * Returns the label associated with the input modifier.
     *
     * @param modifier The modifier to remove.
     * @params annotationsToRemove The annotations to remove.
     *
     * @return The label associated with the input modifier.
     */
    protected String getLabel(String parameterName, String... annotationsToRemove) {
        StringBuilder sb = new StringBuilder();
        sb.append("'@").append(DiagnosticUtils.getSimpleName(annotationsToRemove[0])).append("'");
        for (int i = 1; i < annotationsToRemove.length; i++) {
            sb.append(", '@").append(DiagnosticUtils.getSimpleName(annotationsToRemove[i])).append("'");
        }

        return Messages.getMessage("RemoveTheModifierFromParameter", sb.toString(),
                                   parameterName);
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
        } else if (node.getParent() instanceof MethodDeclaration) {
            return ((MethodDeclaration) node.getParent()).resolveBinding();
        }
        return org.eclipse.jdt.internal.corext.dom.Bindings.getBindingOfParentType(node);
    }

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();
}
