/*******************************************************************************
* Copyright (c) 2021 IBM Corporation.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Hani Damlaj
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.core.cdi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.jdt.codeAction.IJavaCodeActionParticipant;
import org.eclipse.lsp4jakarta.jdt.codeAction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.codeAction.proposal.AddConstructorProposal;
import org.eclipse.lsp4jakarta.jdt.codeAction.proposal.ChangeCorrectionProposal;

/**
 * 
 * Quick fix for adding a `protected`/`public` no argument constructor 
 * for a managed bean that do not have:
 * - a no argument constructor
 * - a constructor annotated with `@Inject`
 *
 */

public class ManagedBeanNoArgConstructorQuickFix  implements IJavaCodeActionParticipant   {

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
            IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);
        if (parentType != null) {
            List<CodeAction> codeActions = new ArrayList<>();
            
            codeActions.addAll(addConstructor(diagnostic, context, parentType));
           

            return codeActions;
        }
        return null;
    }
    
    protected static IBinding getBinding(ASTNode node) {
        if (node.getParent() instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.getParent();
            return ((VariableDeclarationFragment) node.getParent()).resolveBinding();
        }
        return Bindings.getBindingOfParentType(node);
    }
    
    private List<CodeAction> addConstructor(Diagnostic diagnostic, JavaCodeActionContext context, IBinding parentType) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();

        // option for protected constructor
        String name = "Add a no-arg protected constructor to this class";
        ChangeCorrectionProposal proposal = new AddConstructorProposal(name,
                context.getCompilationUnit(), context.getASTRoot(), parentType, 0);
        CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);

        if (codeAction != null) {
            codeActions.add(codeAction);
        }

        // option for public constructor
        name = "Add a no-arg public constructor to this class";
        proposal = new AddConstructorProposal(name,
                context.getCompilationUnit(), context.getASTRoot(), parentType, 0, "public");
        codeAction = context.convertToCodeAction(proposal, diagnostic);

        if (codeAction != null) {
            codeActions.add(codeAction);
        }

        return codeActions;
    }
}
