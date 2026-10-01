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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ReplaceParametersWithSingleParamProposal;

/**
 * Generic abstract quickfix that replaces a method's parameter list so it
 * contains exactly one parameter of a subclass-specified primitive type.
 *
 * <p>Subclasses supply:
 * <ul>
 * <li>{@link #getRequiredPrimitiveType()} — the required primitive type code</li>
 * <li>{@link #getDefaultParamName()} — the name used when inserting a brand-new param</li>
 * <li>{@link #getCodeActionId()} — the unique {@link ICodeActionId} for this action</li>
 * <li>{@link #getLabel()} — the label shown to the user</li>
 * </ul>
 *
 * <p>This class is annotation-agnostic; it delegates all AST rewriting to
 * {@link ReplaceParametersWithSingleParamProposal}.
 */
public abstract class ReplaceParametersWithSingleParamQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(ReplaceParametersWithSingleParamQuickFix.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context,
                                                     Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel());
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));
        return Collections.singletonList(codeAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        IBinding binding = getBinding(context.getCoveredNode());
        ChangeCorrectionProposal proposal = new ReplaceParametersWithSingleParamProposal(getLabel(), context.getCompilationUnit(), context.getASTRoot(), binding, 0, getRequiredPrimitiveType(), getDefaultParamName());
        try {
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
        } catch (CoreException e) {
            LOGGER.log(Level.SEVERE,
                       "Unable to create workspace edit to replace method parameters", e);
        }
        return toResolve;
    }

    /**
     * Returns the named entity (binding) associated with the given AST node.
     *
     * @param node the covered AST node
     * @return the method binding, or the parent-type binding as a fallback
     */
    @SuppressWarnings("restriction")
    protected IBinding getBinding(ASTNode node) {
        if (node.getParent() instanceof MethodDeclaration methodDecl) {
            return methodDecl.resolveBinding();
        }
        return org.eclipse.jdt.internal.corext.dom.Bindings.getBindingOfParentType(node);
    }

    /**
     * Returns the primitive type code that the single method parameter must have.
     *
     * @return the required {@link PrimitiveType.Code}
     */
    protected abstract PrimitiveType.Code getRequiredPrimitiveType();

    /**
     * Returns the parameter name to use when inserting a brand-new parameter
     * (i.e. the method currently has no parameters at all).
     *
     * @return the default parameter name
     */
    protected abstract String getDefaultParamName();

    /**
     * Returns the label shown to the user for this code action.
     *
     * @return the code action label
     */
    protected abstract String getLabel();

    /**
     * Returns the unique {@link ICodeActionId} for this code action.
     *
     * @return the code action id
     */
    protected abstract ICodeActionId getCodeActionId();
}
