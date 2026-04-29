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
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationAttributesQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Removes the 'notifyObserver' attribute from @Observes and @ObservesAsync annotations.
 */
public class RemoveNotifyObserverAttributeQuickFix extends RemoveAnnotationAttributesQuickFix {

    public RemoveNotifyObserverAttributeQuickFix() {
        super(new String[] { Constants.OBSERVES_FQ_NAME, Constants.OBSERVES_ASYNC_FQ_NAME }, "notifyObserver");
    }

    @Override
    public String getParticipantId() {
        return RemoveNotifyObserverAttributeQuickFix.class.getName();
    }

    @Override
    protected ICodeActionId getCodeActionId() {
        return JakartaCodeActionId.CDIRemoveNotifyObserverAttribute;
    }

    @Override
    protected String getLabel(String annotation, String[] attributes) {
        String annotationName = DiagnosticUtils.getSimpleName(annotation);
        return Messages.getMessage("RemoveNotifyObserverAttribute", annotationName);
    }
}
