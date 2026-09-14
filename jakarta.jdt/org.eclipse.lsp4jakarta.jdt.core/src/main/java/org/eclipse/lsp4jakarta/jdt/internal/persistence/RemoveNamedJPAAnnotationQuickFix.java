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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationConflictQuickFix;

import com.google.gson.JsonArray;

/**
 * Removes a named JPA annotation (@NamedEntityGraph, @NamedQuery, @NamedNativeQuery)
 * from the declaring element.
 */
public class RemoveNamedJPAAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    public RemoveNamedJPAAnnotationQuickFix() {
        super(); // annotations are determined dynamically from diagnostic data
    }

    @Override
    public String getParticipantId() {
        return RemoveNamedJPAAnnotationQuickFix.class.getName();
    }

    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.PersistenceRemoveNamedJPAAnnotation;
    }

    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);

        // Receive diagnostic data, set from PersistenceEntityDiagnosticsParticipant
        JsonArray diagnosticData = (JsonArray) diagnostic.getData();
        String[] annotations = StreamSupport.stream(diagnosticData.spliterator(), false).map(e -> e.getAsString()).toArray(String[]::new);

        if (parentType != null) {
            createCodeAction(diagnostic, context, parentType, codeActions, annotations);
        }
        return codeActions;
    }
}
