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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
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
 */
public class NamedEntityGraphDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(NamedEntityGraphDiagnosticsParticipant.class.getName());

    /** {@inheritDoc} */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        long totalStart = System.currentTimeMillis();

        // Phase 1: SearchEngine scan — collect all @NamedEntityGraph names project-wide.
        long scanStart = System.currentTimeMillis();
        Map<String, Integer> projectGraphNameCount = collectProjectGraphNames(context, monitor);
        long scanMs = System.currentTimeMillis() - scanStart;

        // Phase 2: Validate the current compilation unit against the collected names.
        long validateStart = System.currentTimeMillis();
        for (IType type : unit.getAllTypes()) {
            validateType(type, projectGraphNameCount, diagnostics, context);
        }
        long validateMs = System.currentTimeMillis() - validateStart;

        long totalMs = System.currentTimeMillis() - totalStart;

        LOGGER.info(String.format(
                                  "[NamedEntityGraph] collectDiagnostics: file=%s | scan=%d ms | validate=%d ms | total=%d ms | types_scanned=%d | diagnostics=%d",
                                  unit.getElementName(), scanMs, validateMs, totalMs,
                                  projectGraphNameCount.size(), diagnostics.size()));

        return diagnostics;
    }

    /**
     * Validates all {@code @NamedEntityGraph} / {@code @NamedEntityGraphs} annotations
     * on the given type, flagging any whose name appears more than once across the project.
     */
    private void validateType(IType type, Map<String, Integer> projectGraphNameCount,
                              List<Diagnostic> diagnostics, JavaDiagnosticsContext context) throws JavaModelException {

        for (IAnnotation annotation : type.getAnnotations()) {
            String elementName = annotation.getElementName();

            if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPH)) {
                // Single @NamedEntityGraph — inspect its 'name' attribute directly.
                checkGraphAnnotation(annotation, projectGraphNameCount, diagnostics, context);

            } else if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMED_ENTITY_GRAPHS)) {
                // Container @NamedEntityGraphs — iterate nested @NamedEntityGraph annotations.
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

    /**
     * Checks a single {@code @NamedEntityGraph} annotation for a duplicate name.
     */
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

    /**
     * Returns the value of the {@code name} attribute of a {@code @NamedEntityGraph}
     * annotation, or {@code null} if not specified.
     */
    private String getNameAttribute(IAnnotation annotation) throws JavaModelException {
        for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
            if ("name".equals(pair.getMemberName()) && pair.getValue() instanceof String) {
                return (String) pair.getValue();
            }
        }
        return null;
    }

    /**
     * Searches the entire project source scope for all {@code @NamedEntityGraph}
     * declarations and returns a map of graph-name → occurrence count.
     *
     * <p>Uses a class-declaration search to iterate every source type in the project
     * and inspect its annotations directly, which is more reliable than an annotation
     * reference search (whose match element is the annotated member, not the annotation).
     */
    private Map<String, Integer> collectProjectGraphNames(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        Map<String, Integer> nameCount = new HashMap<>();
        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
                                                                    new IJavaProject[] { context.getJavaProject() },
                                                                    IJavaSearchScope.SOURCES);

        // Search for all class/interface declarations in the project source scope.
        SearchPattern pattern = SearchPattern.createPattern(
                                                            "*",
                                                            IJavaSearchConstants.CLASS_AND_INTERFACE,
                                                            IJavaSearchConstants.DECLARATIONS,
                                                            SearchPattern.R_PATTERN_MATCH);

        if (pattern == null) {
            return nameCount;
        }

        // Track how many types the SearchEngine visits during the scan.
        int[] typeCount = { 0 };

        new SearchEngine().search(pattern,
                                  new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
                                  scope,
                                  new SearchRequestor() {
                                      @Override
                                      public void acceptSearchMatch(SearchMatch match) throws CoreException {
                                          Object element = match.getElement();
                                          if (!(element instanceof IType)) {
                                              return;
                                          }
                                          typeCount[0]++;
                                          IType type = (IType) element;
                                          try {
                                              collectGraphNamesFromType(type, nameCount);
                                          } catch (JavaModelException e) {
                                              LOGGER.warning("JavaModelException while scanning type " + type.getElementName()
                                                             + " for @NamedEntityGraph: " + e.getMessage());
                                          }
                                      }
                                  },
                                  monitor);

        LOGGER.info(String.format(
                                  "[NamedEntityGraph] SearchEngine scan complete: types_visited=%d | unique_graph_names_found=%d",
                                  typeCount[0], nameCount.size()));

        return nameCount;
    }

    /**
     * Collects all {@code @NamedEntityGraph} names declared on the given type
     * (via both {@code @NamedEntityGraph} and {@code @NamedEntityGraphs}) into
     * the provided count map.
     */
    private void collectGraphNamesFromType(IType type, Map<String, Integer> nameCount) throws JavaModelException {
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
}
