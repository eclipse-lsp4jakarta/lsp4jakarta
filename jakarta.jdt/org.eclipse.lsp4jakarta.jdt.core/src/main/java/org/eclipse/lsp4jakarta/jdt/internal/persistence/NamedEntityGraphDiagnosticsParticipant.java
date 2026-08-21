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
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.internal.search.JakartaSearchSettings;
import org.eclipse.lsp4jakarta.jdt.internal.search.ProjectWideNameScanner;

/**
 * Persistence diagnostic participant that validates {@code @NamedEntityGraph} usage.
 *
 * <p>Rule enforced:
 * <ul>
 * <li>{@code DuplicateNamedEntityGraphName}: Graph names must be unique within
 * the persistence unit (across all entity classes in the project source scope).
 * Spec §3.7.4:
 * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13662">a13662</a>
 * </li>
 * </ul>
 *
 * <p>The project-wide name collection is delegated entirely to
 * {@link ProjectWideNameScanner}, which owns the scan strategy and performance
 * flag. This participant only supplies the annotation-level extraction logic
 * (what to collect) as a {@code NameExtractorStrategy} lambda.
 */
public class NamedEntityGraphDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(NamedEntityGraphDiagnosticsParticipant.class.getName());

    /** {@inheritDoc} */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(context.getUri());
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        // Guard: skip the expensive project-wide scan entirely unless the current
        // file contains at least one @Entity class that also carries @NamedEntityGraph
        // or @NamedEntityGraphs. This is an O(annotations-in-file) check.
        if (!fileHasEntityWithGraphAnnotation(unit)) {
            return diagnostics;
        }

        // Feature gate: all search-engine-based diagnostics are disabled when
        // JakartaSearchSettings.SEARCH_ENGINE_DIAGNOSTICS_ENABLED is false.
        // This single flag disables every diagnostic that calls ProjectWideNameScanner.
        if (!JakartaSearchSettings.SEARCH_ENGINE_DIAGNOSTICS_ENABLED) {
            return diagnostics;
        }

        long totalStart = System.currentTimeMillis();

        // Phase 1: collect all @NamedEntityGraph names project-wide via the scanner.
        // The extractor lambda is the only @NamedEntityGraph-specific piece of logic;
        // the scan mechanism itself is generic and reusable by other diagnostics.
        long scanStart = System.currentTimeMillis();
        Map<String, Integer> projectGraphNameCount = ProjectWideNameScanner.scan(
                                                                                 context.getJavaProject(), unit,
                                                                                 (type, nameCount) -> extractNamesFromType(type, nameCount),
                                                                                 monitor);
        long scanMs = System.currentTimeMillis() - scanStart;

        // Phase 2: validate types in the current file against the collected counts.
        long validateStart = System.currentTimeMillis();
        for (IType type : unit.getAllTypes()) {
            validateType(type, projectGraphNameCount, diagnostics, context);
        }
        long validateMs = System.currentTimeMillis() - validateStart;

        long totalMs = System.currentTimeMillis() - totalStart;
        LOGGER.info(String.format(
                                  "[NamedEntityGraph] collectDiagnostics: file=%s | scan=%d ms | validate=%d ms | total=%d ms | unique_names=%d | diagnostics=%d",
                                  unit.getElementName(), scanMs, validateMs, totalMs,
                                  projectGraphNameCount.size(), diagnostics.size()));

        return diagnostics;
    }

    // =========================================================================
    // File-level guard
    // =========================================================================

    /**
     * Returns {@code true} if the compilation unit contains at least one type that
     * is annotated with {@code @Entity} <em>and</em> also carries
     * {@code @NamedEntityGraph} or {@code @NamedEntityGraphs}.
     *
     * <p>This is a cheap O(annotations-in-file) check used to skip the expensive
     * project-wide scan for files that cannot possibly produce this diagnostic.
     */
    private boolean fileHasEntityWithGraphAnnotation(ICompilationUnit unit) throws JavaModelException {
        for (IType type : unit.getAllTypes()) {
            boolean hasEntity = false;
            boolean hasGraph = false;
            for (IAnnotation annotation : type.getAnnotations()) {
                String elementName = annotation.getElementName();
                if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.ENTITY)) {
                    hasEntity = true;
                } else if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPH)
                           || DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPHS)) {
                    hasGraph = true;
                }
                if (hasEntity && hasGraph) {
                    return true;
                }
            }
        }
        return false;
    }

    // =========================================================================
    // Extraction logic — supplied to ProjectWideNameScanner as a lambda
    // =========================================================================

    /**
     * Inspects all annotations on {@code type} and merges any {@code @NamedEntityGraph}
     * names (including those nested inside {@code @NamedEntityGraphs}) into
     * {@code nameCount}.
     *
     * <p>This method is passed as the {@link org.eclipse.lsp4jakarta.jdt.internal.search.NameExtractorStrategy}
     * lambda to {@link ProjectWideNameScanner#scan}. It is the only part of this
     * participant that knows about {@code @NamedEntityGraph}.
     */
    private void extractNamesFromType(IType type, Map<String, Integer> nameCount) throws JavaModelException {
        for (IAnnotation annotation : type.getAnnotations()) {
            String elementName = annotation.getElementName();

            if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPH)) {
                String name = getNameAttribute(annotation);
                if (name != null) {
                    nameCount.merge(name, 1, Integer::sum);
                }

            } else if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPHS)) {
                for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
                    if ("value".equals(pair.getMemberName())) {
                        Object val = pair.getValue();
                        Object[] nested = (val instanceof Object[]) ? (Object[]) val : new Object[] { val };
                        for (Object item : nested) {
                            if (item instanceof IAnnotation) {
                                String name = getNameAttribute((IAnnotation) item);
                                if (name != null) {
                                    nameCount.merge(name, 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // Validation — flags duplicates in the current file
    // =========================================================================

    /**
     * Checks all {@code @NamedEntityGraph} / {@code @NamedEntityGraphs} annotations
     * on {@code type} and adds a diagnostic for any whose name appears more than once
     * in the project-wide count map.
     */
    private void validateType(IType type, Map<String, Integer> projectGraphNameCount,
                              List<Diagnostic> diagnostics, JavaDiagnosticsContext context) throws JavaModelException {
        for (IAnnotation annotation : type.getAnnotations()) {
            String elementName = annotation.getElementName();

            if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPH)) {
                checkGraphAnnotation(annotation, projectGraphNameCount, diagnostics, context);

            } else if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPHS)) {
                for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
                    if ("value".equals(pair.getMemberName())) {
                        Object val = pair.getValue();
                        Object[] nested = (val instanceof Object[]) ? (Object[]) val : new Object[] { val };
                        for (Object item : nested) {
                            if (item instanceof IAnnotation) {
                                checkGraphAnnotation((IAnnotation) item, projectGraphNameCount, diagnostics, context);
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkGraphAnnotation(IAnnotation annotation, Map<String, Integer> projectGraphNameCount,
                                      List<Diagnostic> diagnostics, JavaDiagnosticsContext context) throws JavaModelException {
        String graphName = getNameAttribute(annotation);
        if (graphName == null) {
            return;
        }
        Integer count = projectGraphNameCount.get(graphName);
        if (count != null && count > 1) {
            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage("DuplicateNamedEntityGraphName", graphName),
                                                     range, Constants.DIAGNOSTIC_SOURCE,
                                                     ErrorCode.DuplicateNamedEntityGraphName,
                                                     DiagnosticSeverity.Error));
        }
    }

    private String getNameAttribute(IAnnotation annotation) throws JavaModelException {
        for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
            if ("name".equals(pair.getMemberName()) && pair.getValue() instanceof String) {
                return (String) pair.getValue();
            }
        }
        return null;
    }
}
