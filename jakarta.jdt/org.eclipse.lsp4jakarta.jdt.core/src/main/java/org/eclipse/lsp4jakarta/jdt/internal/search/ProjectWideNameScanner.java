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
package org.eclipse.lsp4jakarta.jdt.internal.search;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * Builds a <em>name → occurrence-count</em> map by visiting every source type
 * in the project using a wildcard {@link SearchEngine} class-declaration query.
 *
 * <p>Only called when {@link JakartaSearchSettings#SEARCH_ENGINE_DIAGNOSTICS_ENABLED}
 * is {@code true}; callers must check that flag before invoking this class.
 *
 * <p><b>Performance note:</b> every diagnostic call pays the full scan cost,
 * regardless of how many types actually carry the elements of interest.
 * With a large source set this cost grows linearly with the number of source types.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * Map<String, Integer> counts = ProjectWideNameScanner.scan(
 *                                                           context.getJavaProject(),
 *                                                           (type, nameCount) -> {
 *                                                               for (IAnnotation ann : type.getAnnotations()) {
 *                                                                   if (DiagnosticUtils.isMatchedJavaElement(type, ann.getElementName(),
 *                                                                                                            Constants.NAMED_ENTITY_GRAPH)) {
 *                                                                       String name = getNameAttr(ann);
 *                                                                       if (name != null)
 *                                                                           nameCount.merge(name, 1, Integer::sum);
 *                                                                   }
 *                                                               }
 *                                                           },
 *                                                           monitor);
 * }</pre>
 */
public final class ProjectWideNameScanner {

    private static final Logger LOGGER = Logger.getLogger(ProjectWideNameScanner.class.getName());

    /**
     * Scans every source type in {@code project} and returns a name → count map.
     *
     * <p>Only call this method after confirming
     * {@link JakartaSearchSettings#SEARCH_ENGINE_DIAGNOSTICS_ENABLED} is {@code true}.
     *
     * @param project the Java project whose source scope is searched
     * @param extractor caller-supplied logic — decides what to collect from each type
     * @param monitor progress monitor
     * @return name → occurrence count across all source types in the project
     * @throws CoreException on JDT or search errors
     */
    public static Map<String, Integer> scan(IJavaProject project,
                                            NameExtractorStrategy extractor,
                                            IProgressMonitor monitor) throws CoreException {
        Map<String, Integer> nameCount = new HashMap<>();

        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
                                                                    new IJavaProject[] { project }, IJavaSearchScope.SOURCES);

        SearchPattern pattern = SearchPattern.createPattern(
                                                            "*",
                                                            IJavaSearchConstants.CLASS_AND_INTERFACE,
                                                            IJavaSearchConstants.DECLARATIONS,
                                                            SearchPattern.R_PATTERN_MATCH);

        if (pattern == null) {
            return nameCount;
        }

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
                                          IType type = (IType) element;
                                          try {
                                              extractor.extract(type, nameCount);
                                          } catch (JavaModelException e) {
                                              LOGGER.warning("[ProjectWideNameScanner] JavaModelException on type "
                                                             + type.getElementName() + ": " + e.getMessage());
                                          }
                                      }
                                  }, monitor);

        return nameCount;
    }

    private ProjectWideNameScanner() {
        // utility class — no instances
    }
}
