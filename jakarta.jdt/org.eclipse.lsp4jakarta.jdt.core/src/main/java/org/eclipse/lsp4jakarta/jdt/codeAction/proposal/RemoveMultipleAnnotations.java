/*******************************************************************************
* Copyright (c) 2022 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Adit Rada - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.codeAction.proposal;

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
import org.eclipse.lsp4jakarta.jdt.codeAction.JavaCodeActionContext;
import org.eclipse.lsp4jakarta.jdt.codeAction.proposal.quickfix.RemoveAnnotationConflictQuickFix;

import com.google.gson.JsonArray;

/**
 * This class is used to provide options to remove multiple annotations
 * at the same time. For example, "Remove @A, @B", "Remove @C, @D, @E".
 * 
 * @author Adit Rada
 *
 */
public abstract class RemoveMultipleAnnotations extends RemoveAnnotationConflictQuickFix {
    
    public RemoveMultipleAnnotations() {
        // annotation list to be derived from the diagnostic passed to
        // `getCodeActions()`
        super();
    }
    
    @Override
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
            IProgressMonitor monitor) throws CoreException {
        ASTNode node = context.getCoveredNode();
        IBinding parentType = getBinding(node);

        JsonArray diagnosticData = (JsonArray) diagnostic.getData();

        List<String> annotations = IntStream.range(0, diagnosticData.size())
                .mapToObj(idx -> diagnosticData.get(idx).getAsString()).collect(Collectors.toList());

        if (parentType != null) {
            List<CodeAction> codeActions = new ArrayList<>();
            
            List<List<String>> annotationsListsToRemove = getMultipleRemoveAnnotations(annotations);
            for (List<String> annotationList : annotationsListsToRemove) {
                String[] annotationsToRemove = annotationList.toArray(new String[annotationList.size()]);
                removeAnnotation(diagnostic, context, parentType, codeActions, annotationsToRemove);
            }
            return codeActions;
        }
        return null;
    }
    
    /**
     * Each List in the returned List of Lists should be a set of annotations that
     * will be removed at one go. For example, to proved the user the option to remove
     * "@A, @B" and "@C". The return should be [[A, B], [C]]
     * 
     * @param All the annotations present on the member.
     * @return A List of Lists, with each list containing the annotations that must be
     * removed at the same time.
     * @author Adit Rada
     *
     */
    protected abstract List<List<String>> getMultipleRemoveAnnotations(List<String> annotations);
}
