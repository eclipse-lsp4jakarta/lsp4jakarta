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
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ReplaceAnnotationProposal;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;

import com.google.gson.JsonArray;

/**
 * Abstract base class for quickfixes that replace annotations.
 * Provides common functionality for extracting annotation data from diagnostics
 * and creating code actions that replace multiple annotations with a single annotation.
 */
public abstract class ReplaceAnnotationsQuickFix extends InsertAnnotationMissingQuickFix {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(ReplaceAnnotationsQuickFix.class.getName());

    /** Key for storing annotations to remove in extended data. */
    protected static final String ANNOTATIONS_TO_REMOVE_KEY = "annotationsToRemove";

    /**
     * Constructor.
     *
     * @param annotation The fully qualified name of the annotation to insert
     */
    public ReplaceAnnotationsQuickFix(String annotation) {
        super(annotation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void insertAnnotations(Diagnostic diagnostic, JavaCodeActionContext context,
                                     List<CodeAction> codeActions) throws CoreException {
        // Extract the list of annotations to remove from the diagnostic data
        JsonArray diagnosticData = (JsonArray) diagnostic.getData();
        if (diagnosticData == null || diagnosticData.size() == 0) {
            return;
        }
        List<String> annotationsToRemove = IntStream.range(0, diagnosticData.size()).mapToObj(idx -> diagnosticData.get(idx).getAsString()).collect(Collectors.toList());
        // Format annotation names for display
        String formattedNames = formatAnnotationNames(annotationsToRemove);
        // Create code action
        ExtendedCodeAction codeAction = new ExtendedCodeAction(getCodeActionLabel(formattedNames));
        codeAction.setRelevance(0);
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        codeAction.setKind(CodeActionKind.QuickFix);

        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put(ANNOTATION_KEY, Arrays.asList(getAnnotations()[0]));
        extendedData.put(ANNOTATIONS_TO_REMOVE_KEY, annotationsToRemove);
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

        codeActions.add(codeAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        IBinding parentType = getBinding(context.getCoveredNode());
        ASTNode parentNode = context.getASTRoot().findDeclaringNode(parentType);
        IBinding classBinding = getBinding(parentNode);

        CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();
        List<String> annotationsToRemove = (List<String>) data.getExtendedDataEntry(ANNOTATIONS_TO_REMOVE_KEY);

        if (annotationsToRemove != null && !annotationsToRemove.isEmpty()) {
            // Extract simple names from fully qualified names for ReplaceAnnotationProposal
            String[] simpleNames = annotationsToRemove.stream().map(DiagnosticUtils::getSimpleName).toArray(String[]::new);

            // Format annotation names for display message
            String formattedNames = formatAnnotationNames(annotationsToRemove);
            // Create a proposal that replaces all annotations
            ChangeCorrectionProposal proposal = new ReplaceAnnotationProposal(getCodeActionLabel(formattedNames), context.getCompilationUnit(), context.getASTRoot(), classBinding, 0, getAnnotations()[0], simpleNames);

            try {
                toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
            } catch (CoreException e) {
                LOGGER.log(Level.SEVERE,
                           "Unable to create workspace edit for code action to replace annotations",
                           e);
            }
        }

        return toResolve;
    }

    /**
     * Formats a list of fully qualified annotation names for display.
     * Extracts simple names and joins them with commas and "and" before the last one, prefixed with @.
     *
     * @param annotationFqNames List of fully qualified annotation names
     * @return Formatted string (e.g., "@ApplicationScoped and @RequestScoped" or "@ApplicationScoped, @RequestScoped and @SessionScoped")
     */
    protected String formatAnnotationNames(List<String> annotationFqNames) {
        List<String> simpleNames = annotationFqNames.stream().map(fqName -> "@" + DiagnosticUtils.getSimpleName(fqName)).collect(Collectors.toList());

        int size = simpleNames.size();
        if (size <= 1) {
            return String.join("", simpleNames);
        }

        String allButLast = String.join(", ", simpleNames.subList(0, size - 1));
        return allButLast + " and " + simpleNames.get(size - 1);
    }

    /**
     * Returns the code action label for the given formatted annotation names.
     * Subclasses should override this to provide custom labels.
     *
     * @param formattedNames The formatted annotation names
     * @return The code action label
     */
    protected abstract String getCodeActionLabel(String formattedNames);
}
