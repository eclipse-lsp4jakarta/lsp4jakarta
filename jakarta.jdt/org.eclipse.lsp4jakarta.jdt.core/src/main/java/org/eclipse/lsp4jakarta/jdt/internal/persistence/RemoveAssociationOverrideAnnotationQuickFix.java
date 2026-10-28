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
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.CodeActionUtils;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

/**
 * Removes {@code @AssociationOverride} or {@code @AssociationOverrides} from a
 * class that is not annotated with {@code @Entity}, {@code @MappedSuperclass},
 * or {@code @Embeddable}.
 *
 * <p>One code action is produced per annotation that is actually present on
 * the offending type — so the user sees "Remove @AssociationOverride" when only
 * the single form is present, or "Remove @AssociationOverrides" when only the
 * container form is present.
 */
public class RemoveAssociationOverrideAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * Constructor.
     */
    public RemoveAssociationOverrideAnnotationQuickFix() {
        super(false, Constants.ASSOCIATION_OVERRIDE, Constants.ASSOCIATION_OVERRIDES);
    }

    /**
     * Returns the binding of the type declaration that carries the annotation.
     *
     * <p>The diagnostic range points at the annotation itself, so the covered node
     * is a {@code SimpleName} inside the annotation. We walk up the AST until we
     * reach the owning {@code TypeDeclaration} and return the corresponding
     * type binding. This gives {@link CodeActionUtils#hasAnnotation} the correct
     * binding to inspect.
     */
    @Override
    protected IBinding getBinding(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current instanceof TypeDeclaration) {
                return ((TypeDeclaration) current).resolveBinding();
            }
            current = current.getParent();
        }
        return super.getBinding(node);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overrides the default behaviour so that only a code action for an annotation
     * that is <em>actually present</em> on the covered type is produced. The base
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
        return RemoveAssociationOverrideAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceRemoveAssociationOverrideAnnotation;
    }
}
