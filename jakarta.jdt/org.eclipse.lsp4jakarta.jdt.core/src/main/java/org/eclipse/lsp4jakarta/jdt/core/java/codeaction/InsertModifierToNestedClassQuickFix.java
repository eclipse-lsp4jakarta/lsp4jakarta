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
*    IBM Corporation - initial implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyModifiersProposal;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Add modifiers to the Nested Class.
 */
public abstract class InsertModifierToNestedClassQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(InsertModifierToNestedClassQuickFix.class.getName());

    /**
     * modifier to add.
     */
    private final String modifier;

    /**
     * Constructor.
     *
     * @param modifier The modifier to add.
     */
    public InsertModifierToNestedClassQuickFix(String modifier) {
        this.modifier = modifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context,
                                                     Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);
        if (parentType != null) {
            ExtendedCodeAction codeAction = new ExtendedCodeAction(getLabel(modifier));
            codeAction.setRelevance(0);
            codeAction.setKind(CodeActionKind.QuickFix);
            codeAction.setDiagnostics(Arrays.asList(diagnostic));
            codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), null, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

            codeActions.add(codeAction);
        }

        return codeActions;
    }

    /**
     * {@inheritDoc}
     * Resolves a code action by inserting the appropriate modifier into a nested class matching
     * the annotated field or method parameter type.
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode node = context.getCoveredNode();

        // Case 1: Annotation on a field
        if (node.getParent() instanceof VariableDeclarationFragment) {
            IVariableBinding binding = (IVariableBinding) getBinding(node);
            if (binding != null) {
                ITypeBinding fieldType = binding.getType();
                insertModifier(context, toResolve, fieldType);
            }
        }
        // Case 2: Annotation on a method
        else if (node.getParent() instanceof MethodDeclaration) {
            IMethodBinding methodBinding = (IMethodBinding) getBinding(node);
            for (ITypeBinding paramType : methodBinding.getParameterTypes()) {
                insertModifier(context, toResolve, paramType);
            }
        }

        return toResolve;
    }

    /**
     * insert Modifier to the Node
     * Add modifier to the corresponding node element
     *
     * @param context
     * @param toResolve
     * @param paramType
     * @return
     */
    protected CodeAction insertModifier(
                                        JavaCodeActionResolveContext context,
                                        CodeAction toResolve,
                                        ITypeBinding type) {

        CompilationUnit astRoot = context.getASTRoot();
        ASTNode declNode = astRoot.findDeclaringNode(type);
        if (!(declNode instanceof TypeDeclaration innerDecl))
            return null;

        if (isModifierExist(innerDecl))
            return null;

        try {
            ModifyModifiersProposal proposal = new ModifyModifiersProposal(getLabel(modifier), context.getCompilationUnit(), astRoot, type.getTypeDeclaration(), 0, innerDecl, List.of(modifier));
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
            return toResolve;

        } catch (NoSuchMethodError | IllegalArgumentException | CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to create ModifyModifiersProposal", e);
            return null;
        }
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
     * Returns the label associated with the input modifier.
     *
     * @param modifier The modifier to add.
     * @return The label associated with the input modifier.
     */
    protected String getLabel(String modifier) {
        return Messages.getMessage("InsertModifierToNestedClass", modifier);
    }

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();

    /**
     * isModifierExist
     * This check verifies whether the modifier already exists on the node.
     *
     * @param typeDeclaration
     * @return
     */
    protected abstract boolean isModifierExist(TypeDeclaration typeDeclaration);
}
