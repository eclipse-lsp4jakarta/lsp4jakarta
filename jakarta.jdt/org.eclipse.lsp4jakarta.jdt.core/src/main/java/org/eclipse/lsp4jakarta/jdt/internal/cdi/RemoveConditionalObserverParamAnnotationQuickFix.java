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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Removes the @Observes or @ObservesAsync annotations from method parameters
 * when they are conditional observers on @Dependent scoped beans.
 */
public class RemoveConditionalObserverParamAnnotationQuickFix extends RemoveMethodParamAnnotationQuickFix {

    public RemoveConditionalObserverParamAnnotationQuickFix() {
        super(Constants.OBSERVES_FQ_NAME, Constants.OBSERVES_ASYNC_FQ_NAME);
    }

    @Override
    public String getParticipantId() {
        return RemoveConditionalObserverParamAnnotationQuickFix.class.getName();
    }

    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveConditionalObserverAnnotations;
    }

    @Override
    protected String getLabel(String parameterName, String... annotationsToRemove) {
        String annotationName = "'@" + DiagnosticUtils.getSimpleName(annotationsToRemove[0]) + "'";
        return Messages.getMessage("RemoveConditionalObserverParamAnnotation", annotationName, parameterName);
    }
}
