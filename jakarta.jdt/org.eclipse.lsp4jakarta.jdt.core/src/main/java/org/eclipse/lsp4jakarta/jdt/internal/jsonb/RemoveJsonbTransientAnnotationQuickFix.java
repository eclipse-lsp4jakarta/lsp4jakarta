/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
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
 * Removes the JsonbTransient annotation from declaring element.
 */
public class RemoveJsonbTransientAnnotationQuickFix extends RemoveAnnotationConflictQuickFix {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveJsonbTransientAnnotationQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JakartaCodeActionId getCodeActionId() {
        return JakartaCodeActionId.JSONBRemoveJsonbTransientAnnotation;
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
            if (annotations.contains(Constants.JSONB_TRANSIENT_FQ_NAME)) {
                createCodeAction(diagnostic, context, parentType, codeActions,
                                 "jakarta.json.bind.annotation.JsonbTransient");
            }
        }

        return codeActions;
    }
}
