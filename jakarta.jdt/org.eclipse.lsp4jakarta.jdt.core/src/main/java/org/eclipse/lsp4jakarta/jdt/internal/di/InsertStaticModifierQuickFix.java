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
package org.eclipse.lsp4jakarta.jdt.internal.di;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyModifiersProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Insert the static modifier to the Nested class.
 */
public class InsertStaticModifierQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(InsertStaticModifierQuickFix.class.getName());

    /**
     * Constructor.
     */
    public InsertStaticModifierQuickFix() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context,
                                                     Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveredNode();
        List<CodeAction> codeActions = new ArrayList<>();
        if (node == null) {
            return codeActions;
        }
        IBinding parentType = getBinding(node);
        if (parentType != null) {
            ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel());
            codeAction.setRelevance(0);
            codeAction.setKind(CodeActionKind.QuickFix);
            codeAction.setDiagnostics(Arrays.asList(diagnostic));
            codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

            codeActions.add(codeAction);
        }

        return codeActions;
    }

    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode node = context.getCoveredNode();
        if (node == null) {
            return toResolve;
        }
        // Case 1: @Inject on a field

        if (node.getParent() instanceof VariableDeclarationFragment) {
            IVariableBinding binding = (IVariableBinding) getBinding(node);
            if (binding != null) {
                ITypeBinding fieldType = binding.getType();
                makeMemberTypeStaticIfNeeded(context, toResolve, fieldType);
            }
        }
        // Case 2: @Inject on a method
        else if (node.getParent() instanceof MethodDeclaration) {
            IMethodBinding methodBinding = (IMethodBinding) getBinding(node);
            for (ITypeBinding paramType : methodBinding.getParameterTypes()) {
                makeMemberTypeStaticIfNeeded(context, toResolve, paramType);
            }
        }

        return toResolve;
    }

    private CodeAction makeMemberTypeStaticIfNeeded(
                                                    JavaCodeActionResolveContext context,
                                                    CodeAction toResolve,
                                                    ITypeBinding innerType) {

        CompilationUnit astRoot = context.getASTRoot();
        ASTNode declNode = astRoot.findDeclaringNode(innerType);
        if (!(declNode instanceof TypeDeclaration innerDecl))
            return null;

        if (Modifier.isStatic(innerDecl.getModifiers()))
            return null;

        try {
            ModifyModifiersProposal proposal = new ModifyModifiersProposal(getLabel(), context.getCompilationUnit(), astRoot, innerType.getTypeDeclaration(), 0, innerDecl, List.of("static"));
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
            return toResolve;

        } catch (NoSuchMethodError | IllegalArgumentException | CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to create ModifyModifiersProposal", e);
            return null;
        }
    }

    protected String getLabel() {
        return Messages.getMessage("MakeInnerClassStatic");
    }

    /**
     * {@inheritDoc}
     */
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.MakeMethodStatic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return InsertStaticModifierQuickFix.class.getName();
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

}
