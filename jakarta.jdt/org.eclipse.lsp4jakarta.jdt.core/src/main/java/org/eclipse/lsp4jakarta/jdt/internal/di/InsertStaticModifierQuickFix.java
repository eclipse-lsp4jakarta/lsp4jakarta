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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.InsertModifierToNestedClassQuickFix;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionResolveContext;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyModifiersProposal;

/**
 * Insert the static modifier.
 */
public class InsertStaticModifierQuickFix extends InsertModifierToNestedClassQuickFix {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(InsertStaticModifierQuickFix.class.getName());

    /**
     * Constructor.
     */
    public InsertStaticModifierQuickFix() {
        super("static");
    }

    /**
     * {@inheritDoc}
     */
    protected CodeAction insertModifier(
                                        JavaCodeActionResolveContext context,
                                        CodeAction toResolve,
                                        ITypeBinding type) {

        CompilationUnit astRoot = context.getASTRoot();
        ASTNode declNode = astRoot.findDeclaringNode(type);
        if (!(declNode instanceof TypeDeclaration innerDecl))
            return null;

        if (Modifier.isStatic(innerDecl.getModifiers()))
            return null;

        try {
            ModifyModifiersProposal proposal = new ModifyModifiersProposal(getLabel("static"), context.getCompilationUnit(), astRoot, type.getTypeDeclaration(), 0, innerDecl, List.of("static"));
            toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
            return toResolve;

        } catch (NoSuchMethodError | IllegalArgumentException | CoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to create ModifyModifiersProposal", e);
            return null;
        }
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

}
