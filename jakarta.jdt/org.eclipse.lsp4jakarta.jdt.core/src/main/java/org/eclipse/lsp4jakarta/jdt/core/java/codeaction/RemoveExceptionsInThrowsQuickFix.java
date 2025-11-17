/*******************************************************************************
* Copyright (c) 2025 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyExceptionsInThrowsProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Removes Exceptions from the active method.
 */
public abstract class RemoveExceptionsInThrowsQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(RemoveExceptionsInThrowsQuickFix.class.getName());

    private String messageIdentifier;

    public static final String EXCEPTIONS_TYPE = "exceptions.name";

    /**
     * Constructor.
     *
     * @param messageIdentifier.
     */
    public RemoveExceptionsInThrowsQuickFix(String messageIdentifier) {
        this.messageIdentifier = messageIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveExceptionsInThrowsQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        JsonArray diagnosticData = (JsonArray) diagnostic.getData();
        List<String> exceptions = getExceptions(diagnosticData);
        ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel());
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        Map<String, Object> extendedData = new HashMap<String, Object>();
        extendedData.put(EXCEPTIONS_TYPE, exceptions);
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));
        return Collections.singletonList(codeAction);
    }

    /**
     * getExceptions
     *
     * @param diagnosticData
     * @return Get the exception list from diagnosticData
     */
    private List<String> getExceptions(JsonArray diagnosticData) {
        List<String> exceptions = new ArrayList<>(diagnosticData.size());
        for (JsonElement element : diagnosticData) {
            exceptions.add(element.getAsString());
        }
        return exceptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();
        List<String> exceptions = (List<String>) data.getExtendedDataEntry(EXCEPTIONS_TYPE);
        ASTNode node = context.getCoveredNode();
        MethodDeclaration parentNode = (MethodDeclaration) node.getParent();
        IMethodBinding parentMethod = parentNode.resolveBinding();
        List<Type> exceptionsToRemove = getFilteredExceptions(parentNode, exceptions);
        ChangeCorrectionProposal proposal = new ModifyExceptionsInThrowsProposal(getLabel(), context.getCompilationUnit(), context.getASTRoot(), parentMethod, 0, exceptionsToRemove, null);

        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to create workspace edit to remove all parameters", e);
        }

        return toResolve;
    }

    /**
     * getFilteredExceptions
     *
     * @param parentNode
     * @param exceptions
     * @return exceptions that need to be removed from the throws clause.
     */
    private List<Type> getFilteredExceptions(MethodDeclaration parentNode, List<String> exceptions) {
        List<Type> exceptionsDeclared = (List<Type>) parentNode.thrownExceptionTypes();
        List<Type> filteredExceptions = exceptionsDeclared.stream().filter(type -> {
            ITypeBinding binding = type.resolveBinding();
            return binding != null && exceptions.contains(binding.getQualifiedName());
        }).collect(Collectors.toList());
        return filteredExceptions;
    }

    /**
     * Returns the code action label.
     *
     * @param messageIdentifier
     *
     * @return The code action label.
     */
    public String getLabel() {
        return Messages.getMessage(messageIdentifier);
    }

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();
}
