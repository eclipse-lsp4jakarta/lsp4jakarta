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
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.CodeActionUtils;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

/**
 * Removes {@code @AttributeOverride} or {@code @AttributeOverrides} from a
 * field or property that is not annotated with {@code @Embedded},
 * {@code @EmbeddedId}, or {@code @ElementCollection}.
 *
 * <p>One code action is produced per annotation that is actually present on
 * the offending member — so the user sees "Remove @AttributeOverride" when only
 * the single form is present, or "Remove @AttributeOverrides" when only the
 * container form is present.
 */
public class RemoveAttributeOverrideAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveAttributeOverrideAnnotationQuickFix() {
        super(false, Constants.ATTRIBUTE_OVERRIDE, Constants.ATTRIBUTE_OVERRIDES);
    }

    /**
     * Returns the binding of the field or method that carries the annotation.
     *
     * <p>The diagnostic range points at the annotation itself, so the covered node
     * is a {@code SimpleName} inside the annotation. We walk up the AST until we
     * reach the owning {@code FieldDeclaration} or {@code MethodDeclaration} and
     * return the corresponding field/method binding. This gives
     * {@link CodeActionUtils#hasAnnotation} the correct binding to inspect.
     */
    @Override
    protected IBinding getBinding(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current instanceof FieldDeclaration) {
                FieldDeclaration fieldDecl = (FieldDeclaration) current;
                if (!fieldDecl.fragments().isEmpty()) {
                    return ((VariableDeclarationFragment) fieldDecl.fragments().get(0)).resolveBinding();
                }
            } else if (current instanceof MethodDeclaration) {
                return ((MethodDeclaration) current).resolveBinding();
            }
            current = current.getParent();
        }
        return super.getBinding(node);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overrides the default behaviour so that only a code action for an annotation
     * that is <em>actually present</em> on the covered member is produced. The base
     * class would otherwise generate one action for every registered annotation
     * regardless of whether it appears on the element.
     */
    @Override
    protected void createCodeActions(Diagnostic diagnostic, JavaCodeActionContext context,
                                     IBinding parentType, List<CodeAction> codeActions) throws CoreException {
        for (String annotationFqn : getAnnotations()) {
            if (CodeActionUtils.hasAnnotation(parentType, annotationFqn)) {
                createCodeAction(diagnostic, context, parentType, codeActions, annotationFqn);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveAttributeOverrideAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceRemoveAttributeOverrideAnnotation;
    }
}
