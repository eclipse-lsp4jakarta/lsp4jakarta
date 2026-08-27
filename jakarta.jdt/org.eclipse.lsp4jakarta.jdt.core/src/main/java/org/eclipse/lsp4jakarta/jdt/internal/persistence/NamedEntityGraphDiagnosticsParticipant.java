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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.internal.search.AnnotationConsumer;
import org.eclipse.lsp4jakarta.jdt.internal.search.JakartaSearchSettings;
import org.eclipse.lsp4jakarta.jdt.internal.search.ProjectWideNameScanner;

/**
 * Validates that {@code @NamedEntityGraph} names are unique within the persistence unit.
 * Spec §3.7.4:
 * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13662">a13662</a>
 */
public class NamedEntityGraphDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /** {@inheritDoc} */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        ICompilationUnit unit = JDTUtilsLSImpl.getInstance().resolveCompilationUnit(context.getUri());
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }
        // Guard: O(annotations-in-file) — skip the expensive scan if this file has
        // no @Entity class that also carries @NamedEntityGraph or @NamedEntityGraphs.
        if (!fileHasEntityWithGraphAnnotation(unit)) {
            return diagnostics;
        }
        // Feature gate: disabled globally → no scan, no diagnostics.
        if (!JakartaSearchSettings.SEARCH_ENGINE_DIAGNOSTICS_ENABLED) {
            return diagnostics;
        }

        Map<String, Integer> counts = ProjectWideNameScanner.scan(
                                                                  context.getJavaProject(), this::extractNamesFromType, monitor);

        for (IType type : unit.getAllTypes()) {
            validateType(type, counts, diagnostics, context);
        }
        return diagnostics;
    }

    // -------------------------------------------------------------------------
    // File-level guard
    // -------------------------------------------------------------------------

    private boolean fileHasEntityWithGraphAnnotation(ICompilationUnit unit) throws JavaModelException {
        for (IType type : unit.getAllTypes()) {
            boolean hasEntity = false;
            boolean hasGraph = false;
            for (IAnnotation ann : type.getAnnotations()) {
                String name = ann.getElementName();
                if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.ENTITY)) {
                    hasEntity = true;
                }
                if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.NAMED_ENTITY_GRAPH)
                    || DiagnosticUtils.isMatchedJavaElement(type, name, Constants.NAMED_ENTITY_GRAPHS)) {
                    hasGraph = true;
                }
                if (hasEntity && hasGraph) {
                    return true;
                }
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Extraction — passed to ProjectWideNameScanner as a method reference
    // -------------------------------------------------------------------------

    private void extractNamesFromType(IType type, Map<String, Integer> nameCount) throws JavaModelException {
        for (IAnnotation ann : type.getAnnotations()) {
            String elementName = ann.getElementName();
            if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPH)) {
                mergeGraphName(ann, nameCount);
            } else if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPHS)) {
                forEachNestedGraph(ann, nested -> mergeGraphName(nested, nameCount));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Validation — flags duplicates in the current file
    // -------------------------------------------------------------------------

    private void validateType(IType type, Map<String, Integer> counts,
                              List<Diagnostic> diagnostics, JavaDiagnosticsContext context) throws JavaModelException {
        for (IAnnotation ann : type.getAnnotations()) {
            String elementName = ann.getElementName();
            if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPH)) {
                checkForDuplicate(ann, counts, diagnostics, context);
            } else if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPHS)) {
                forEachNestedGraph(ann, nested -> checkForDuplicate(nested, counts, diagnostics, context));
            }
        }
    }

    private void checkForDuplicate(IAnnotation ann, Map<String, Integer> counts,
                                   List<Diagnostic> diagnostics, JavaDiagnosticsContext context) throws JavaModelException {
        String graphName = DiagnosticUtils.getAnnotationMemberValue(ann, "name", String.class);
        if (graphName != null && counts.getOrDefault(graphName, 0) > 1) {
            Range range = PositionUtils.toNameRange(ann, context.getUtils());
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage("DuplicateNamedEntityGraphName", graphName),
                                                     range, Constants.DIAGNOSTIC_SOURCE,
                                                     ErrorCode.DuplicateNamedEntityGraphName,
                                                     DiagnosticSeverity.Error));
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private void mergeGraphName(IAnnotation ann, Map<String, Integer> nameCount) throws JavaModelException {
        String name = DiagnosticUtils.getAnnotationMemberValue(ann, "name", String.class);
        if (name != null) {
            nameCount.merge(name, 1, Integer::sum);
        }
    }

    private void forEachNestedGraph(IAnnotation container, AnnotationConsumer consumer) throws JavaModelException {
        Object val = DiagnosticUtils.getAnnotationMemberValue(container, "value", Object.class);
        if (val == null) {
            return;
        }
        Object[] items = (val instanceof Object[]) ? (Object[]) val : new Object[] { val };
        for (Object item : items) {
            if (item instanceof IAnnotation) {
                consumer.accept((IAnnotation) item);
            }
        }
    }
}
