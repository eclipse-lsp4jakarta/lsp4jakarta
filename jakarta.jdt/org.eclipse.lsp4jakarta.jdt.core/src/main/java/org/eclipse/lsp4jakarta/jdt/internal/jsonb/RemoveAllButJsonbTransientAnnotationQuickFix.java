/*******************************************************************************
* Copyright (c) 2023, 2025 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.jsonb;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
 * Removes all annotations with the exception of @JsonbTransient from declaring
 * element.
 */
public class RemoveAllButJsonbTransientAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveAllButJsonbTransientAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.JSONBRemoveAllButJsonbTransientAnnotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);
        if (parentType != null) {
            JsonArray diagnosticData = (JsonArray) diagnostic.getData();
            List<String> annotations = IntStream.range(0, diagnosticData.size()).mapToObj(idx -> diagnosticData.get(idx).getAsString()).collect(Collectors.toList());

            annotations.remove(Constants.JSONB_TRANSIENT_FQ_NAME);
            if (annotations.size() > 0) {
                createCodeAction(diagnostic, context, parentType, codeActions, annotations.toArray(new String[0]));
            }
        }

        return codeActions;
    }
}
