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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.commons.codeaction.JakartaCodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.java.codeaction.RemoveAnnotationAttributesQuickFix;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Removes the 'notifyObserver' attribute from @Observes and @ObservesAsync annotations
 * when it has the value Reception.IF_EXISTS (conditional observer on @Dependent scoped beans).
 */
public class RemoveNotifyObserverAttributeQuickFix extends RemoveAnnotationAttributesQuickFix {

    public RemoveNotifyObserverAttributeQuickFix() {
        super(createAnnotationAttributesMap(), true);
    }

    private static Map<String, List<String>> createAnnotationAttributesMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(Constants.OBSERVES_FQ_NAME, List.of("notifyObserver"));
        map.put(Constants.OBSERVES_ASYNC_FQ_NAME, List.of("notifyObserver"));
        return map;
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
