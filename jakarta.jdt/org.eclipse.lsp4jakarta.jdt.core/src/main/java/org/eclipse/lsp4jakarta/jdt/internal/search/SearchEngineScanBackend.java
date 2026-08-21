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

import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
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
 * {@link ScanBackend} that visits <em>every</em> source type in the project using
 * a wildcard {@link SearchEngine} class-declaration query.
 *
 * <p><b>Performance note:</b> every diagnostic call pays the full scan cost,
 * regardless of how many types actually carry the elements of interest.
 * With a large source set (e.g. 500+ annotated entity classes) this cost is
 * measurable and grows linearly with the number of source types — this is the
 * performance bottleneck that {@link ProjectWideNameScanner} is designed to expose
 * and allow comparison against {@link CompilationUnitScanBackend}.
 */
public class SearchEngineScanBackend implements ScanBackend {

    private static final Logger LOGGER = Logger.getLogger(SearchEngineScanBackend.class.getName());

    @Override
    public void visitTypes(IJavaProject project,
                           ICompilationUnit unit,
                           NameExtractorStrategy extractor,
                           Map<String, Integer> nameCount,
                           IProgressMonitor monitor) throws CoreException {

        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
                                                                    new IJavaProject[] { project }, IJavaSearchScope.SOURCES);

        SearchPattern pattern = SearchPattern.createPattern(
                                                            "*",
                                                            IJavaSearchConstants.CLASS_AND_INTERFACE,
                                                            IJavaSearchConstants.DECLARATIONS,
                                                            SearchPattern.R_PATTERN_MATCH);

        if (pattern == null) {
            return;
        }

        int[] typesVisited = { 0 };

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
                                          typesVisited[0]++;
                                          IType type = (IType) element;
                                          try {
                                              extractor.extract(type, nameCount);
                                          } catch (JavaModelException e) {
                                              LOGGER.warning("[SearchEngineScanBackend] JavaModelException on type "
                                                             + type.getElementName() + ": " + e.getMessage());
                                          }
                                      }
                                  }, monitor);

        LOGGER.info(String.format(
                                  "[ProjectWideNameScanner][SearchEngine] types_visited=%d | unique_names=%d",
                                  typesVisited[0], nameCount.size()));
    }
}
