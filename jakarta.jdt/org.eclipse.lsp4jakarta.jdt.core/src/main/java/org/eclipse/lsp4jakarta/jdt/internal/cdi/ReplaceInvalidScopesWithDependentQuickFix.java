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
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ReplaceAnnotationProposal;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

import com.google.gson.JsonArray;

/**
 * Quick fix to replace invalid scope annotations on interceptors/decorators with @Dependent.
 * Interceptors and decorators must only use @Dependent scope, not normal scopes.
 */
public class ReplaceInvalidScopesWithDependentQuickFix extends InsertAnnotationMissingQuickFix {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(ReplaceInvalidScopesWithDependentQuickFix.class.getName());

    /**
     * Constructor.
     */
    public ReplaceInvalidScopesWithDependentQuickFix() {
        super(Constants.DEPENDENT_FQ_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return ReplaceInvalidScopesWithDependentQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIReplaceInvalidScopesWithDependent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void insertAnnotations(Diagnostic diagnostic, JavaCodeActionContext context,
                                     List<CodeAction> codeActions) throws CoreException {
        // Create a single code action to replace all invalid scopes with @Dependent
        insertAnnotation(diagnostic, context, codeActions, Constants.DEPENDENT_FQ_NAME);
    }

    /**
     * Adds a code action to replace invalid scope annotations with @Dependent.
     *
     * @param diagnostic The diagnostic associated with this action.
     * @param context The context.
     * @param codeActions The list of code actions.
     * @param annotation The annotation to insert (@Dependent).
     * @throws CoreException
     */
    protected void insertAnnotation(Diagnostic diagnostic, JavaCodeActionContext context, List<CodeAction> codeActions,
                                    String annotation) throws CoreException {
        // Extract invalid scopes from diagnostic data
        JsonArray diagnosticData = (JsonArray) diagnostic.getData();
        List<String> invalidScopes = IntStream.range(0, diagnosticData.size()).mapToObj(idx -> diagnosticData.get(idx).getAsString()).collect(Collectors.toList());

        // Format scope names for display (e.g., "@ApplicationScoped, @SessionScoped")
        String scopeNames = invalidScopes.stream().map(scope -> "@" + DiagnosticUtils.getSimpleName(scope)).collect(Collectors.joining(", "));

        String name = Messages.getMessage("ReplaceInvalidScopesWithDependent", scopeNames);
        ExtendedCodeAction codeAction = new ExtendedCodeAction(name);
        codeAction.setRelevance(0);
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        codeAction.setKind(CodeActionKind.QuickFix);

        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put(ANNOTATION_KEY, Arrays.asList(annotation));
        extendedData.put("invalidScopes", invalidScopes);
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

        codeActions.add(codeAction);
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
        List<String> resolveAnnotations = (List<String>) data.getExtendedDataEntry(ANNOTATION_KEY);
        List<String> invalidScopes = (List<String>) data.getExtendedDataEntry("invalidScopes");

        String[] resolveAnnotationsArray = resolveAnnotations.toArray(String[]::new);
        String[] invalidScopesArray = invalidScopes != null ? invalidScopes.toArray(String[]::new) : new String[0];

        // Only a single annotation insertion is expected (@Dependent)
        if (resolveAnnotationsArray.length == 1) {
            String annotation = resolveAnnotationsArray[0];
            String name = Messages.getMessage("ReplaceInvalidScopesWithDependent");

            // Create proposal to replace all invalid scopes with @Dependent
            ChangeCorrectionProposal proposal = new ReplaceAnnotationProposal(name, context.getCompilationUnit(), context.getASTRoot(), parentType, 0, annotation, invalidScopesArray);
            try {
                toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
            } catch (CoreException e) {
                LOGGER.log(Level.SEVERE,
                           "Unable to create workspace edit for code action to replace invalid scopes with @Dependent", e);
            }
        }

        return toResolve;
    }
}

// Made with Bob
