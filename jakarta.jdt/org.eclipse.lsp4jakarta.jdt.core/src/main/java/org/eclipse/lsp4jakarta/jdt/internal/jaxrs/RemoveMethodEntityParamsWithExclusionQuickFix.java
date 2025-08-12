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
package org.eclipse.lsp4jakarta.jdt.internal.jaxrs;

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
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.RemoveParamsProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Removes a resource method's entity parameters with the exception of the
 * currently selected element representing the parameter to keep.
 */
public class RemoveMethodEntityParamsWithExclusionQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(RemoveMethodEntityParamsWithExclusionQuickFix.class.getName());

    /** Map key to retrieve an entity parameter name. */
    public static final String ENTITY_PARAM_NAME_TO_KEEP_ID_KEY = "entity.param.to.keep.identifier";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveMethodEntityParamsWithExclusionQuickFix.class.getName();
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
        IMethodBinding parentMethod = parentNode.resolveBinding();

        if (parentMethod != null) {
            Tuple.Two<List<SingleVariableDeclaration>, SingleVariableDeclaration> entityParams = getEntityParams(
                                                                                                                 context, null);

            for (SingleVariableDeclaration entityParam : entityParams.getFirst()) {
                ExtendedCodeAction codeAction = createCodeAction(context, diagnostic, entityParam);
                codeActions.add(codeAction);
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
        String entityParamToKeepId = (String) data.getExtendedDataEntry(ENTITY_PARAM_NAME_TO_KEEP_ID_KEY);
        Tuple.Two<List<SingleVariableDeclaration>, SingleVariableDeclaration> dataTuple = getEntityParams(context,
                                                                                                          entityParamToKeepId);
        List<SingleVariableDeclaration> entityParams = dataTuple.getFirst();
        SingleVariableDeclaration entityParamToKeep = dataTuple.getSecond();
        String label = getLabel(entityParamToKeepId);
        ChangeCorrectionProposal proposal = new RemoveParamsProposal(label, context.getCompilationUnit(), context.getASTRoot(), parentMethod, 0, entityParams, entityParamToKeep);

        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve code action edit to remove entity parameters", e);
        }

        return toResolve;
    }

    /**
     * Creates a code action.
     *
     * @param context The code action context.
     * @param diagnostic The diagnostic associated to this code action.
     * @param enityParamToKeep The entity parameter to keep.
     *
     * @return a code action.
     */
    private ExtendedCodeAction createCodeAction(JavaCodeActionContext context, Diagnostic diagnostic,
                                                SingleVariableDeclaration enityParamToKeep) {
        String paramId = getIdentifier(enityParamToKeep);
        String label = getLabel(paramId);
        ICodeActionId id = JakartaCodeActionId.RemoveAllEntityParametersExcept;
        ExtendedCodeAction codeAction = new ExtendedCodeAction(label);
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        Map<String, Object> extendedData = new HashMap<String, Object>();
        extendedData.put(ENTITY_PARAM_NAME_TO_KEEP_ID_KEY, paramId);

        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), id));

        return codeAction;

    }

    /**
     * Returns a Tuple object containing a list of entity parameters and the entity
     * parameter associated with the
     * input search parameter identifier.
     *
     * @param context The code action context.
     * @param searchParmId The inpust search parameter identifier. It can be null;
     *
     * @return The Tuple object containing a list of entity parameters and the
     *         entity parameter associated with the
     *         input search parameter identifier.
     */
    private Tuple.Two<List<SingleVariableDeclaration>, SingleVariableDeclaration> getEntityParams(
                                                                                                  JavaCodeActionContext context, String searchParmId) {

        ASTNode node = context.getCoveredNode();
        MethodDeclaration parentNode = (MethodDeclaration) node.getParent();
        List<SingleVariableDeclaration> entityParams = new ArrayList<SingleVariableDeclaration>();

        List<SingleVariableDeclaration> params = (List<SingleVariableDeclaration>) parentNode.parameters();
        SingleVariableDeclaration foundId = null;
        for (SingleVariableDeclaration param : params) {
            if (isEntityParam(param)) {
                if (foundId == null && searchParmId != null && getIdentifier(param).equals(searchParmId)) {
                    foundId = param;
                }
                entityParams.add(param);
            }
        }

        return Tuple.two(entityParams, foundId);
    }

    /**
     * Returns a boolean variable that indicates whether the given parameter is an
     * entity parameter or not.
     *
     * @param param the parameter to check whether it is an entity parameter or not
     * @return true if the given parameter is an entity parameter, false otherwise
     */
    private boolean isEntityParam(SingleVariableDeclaration param) {
        ArrayList<String> nonEntityParamAnnotations = new ArrayList<>(Arrays.asList(Constants.SET_OF_NON_ENTITY_PARAM_ANNOTATIONS));
        boolean isEntityParam = true;
        List<?> modifiers = param.modifiers();
        for (Object o : modifiers) {
            IExtendedModifier modifier = (IExtendedModifier) o;
            if (modifier.isAnnotation()) {
                Annotation annotation = (Annotation) modifier;
                ITypeBinding binding = annotation.resolveTypeBinding();
                if (nonEntityParamAnnotations.contains(binding.getQualifiedName())) {
                    isEntityParam = false;
                    break;
                }
            }
        }

        return isEntityParam;
    }

    /**
     * Returns the code action label.
     *
     * @param identifier The identifier to be associated to the label.
     *
     * @return The code action label.
     */
    private String getLabel(String identifier) {
        return Messages.getMessage("RemoveAllEntityParametersExcept", identifier);
    }

    /**
     * Returns the identifier associated with the input parameter object.
     *
     * @param entityParam The entity parameter.
     *
     * @return The identifier associated with the input parameter object.
     */
    private String getIdentifier(SingleVariableDeclaration entityParam) {
        return entityParam.getName().getIdentifier();
    }
}
