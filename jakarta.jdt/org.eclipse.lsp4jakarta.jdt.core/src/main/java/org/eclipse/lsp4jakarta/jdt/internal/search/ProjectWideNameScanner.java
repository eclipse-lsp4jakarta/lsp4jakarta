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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Entry point for building a <em>name → occurrence-count</em> map by visiting
 * Java types across the project.
 *
 * <h3>How it works</h3>
 * <p>Two orthogonal concerns are kept separate and independently swappable:
 * <ol>
 * <li><b>{@link NameExtractorStrategy}</b> — <em>what</em> to collect.
 * Supplied by the caller; completely controls which element level
 * (class annotation, field annotation, method annotation, or any
 * combination) is inspected and which string value is counted.</li>
 * <li><b>{@link ScanBackend}</b> — <em>which</em> types to visit.
 * Two built-in implementations are provided:
 * <ul>
 * <li>{@link SearchEngineScanBackend} — visits every source type in the
 * project index. Correct for cross-file uniqueness rules; cost grows
 * with project size. <b>This is the performance bottleneck under
 * study.</b></li>
 * <li>{@link CompilationUnitScanBackend} — visits only types in the
 * current file. O(1) cost; used as a fast baseline or when
 * cross-file detection is not needed.</li>
 * </ul>
 * Custom backends can be supplied directly to the explicit overload of
 * {@link #scan}.</li>
 * </ol>
 *
 * <h3>Usage — class-level annotation (e.g. {@code @NamedEntityGraph})</h3>
 *
 * <pre>{@code
 * Map<String, Integer> counts = ProjectWideNameScanner.scan(
 *                                                           context.getJavaProject(), unit,
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
 *
 * <h3>Usage — field-level annotation (e.g. {@code @Inject} uniqueness)</h3>
 *
 * <pre>{@code
 * Map<String, Integer> counts = ProjectWideNameScanner.scan(
 *                                                           context.getJavaProject(), unit,
 *                                                           (type, nameCount) -> {
 *                                                               for (IField field : type.getFields()) {
 *                                                                   if (DiagnosticUtils.isMatchedAnnotation(unit, field.getAnnotations(), INJECT)) {
 *                                                                       nameCount.merge(field.getElementName(), 1, Integer::sum);
 *                                                                   }
 *                                                               }
 *                                                           },
 *                                                           monitor);
 * }</pre>
 *
 * <h3>Plugging in a custom backend</h3>
 * <p>Implement {@link ScanBackend} and pass it to the explicit overload:
 *
 * <pre>{@code
 * ProjectWideNameScanner.scan(project, unit, extractor, myCustomBackend, monitor);
 * }</pre>
 */
public final class ProjectWideNameScanner {

    private static final Logger LOGGER = Logger.getLogger(ProjectWideNameScanner.class.getName());

    /**
     * Shared instance of the SearchEngine-based backend.
     * Use via {@link #scan} or pass directly to the explicit-backend overload.
     */
    public static final ScanBackend SEARCH_ENGINE_BACKEND = new SearchEngineScanBackend();

    /**
     * Shared instance of the compilation-unit-only backend.
     * Use via {@link #scan} (with {@link #USE_SEARCH_ENGINE}{@code = false}) or
     * pass directly to the explicit-backend overload.
     */
    public static final ScanBackend COMPILATION_UNIT_BACKEND = new CompilationUnitScanBackend();

    /**
     * Controls which built-in backend {@link #scan} selects when no backend is
     * specified explicitly.
     *
     * <ul>
     * <li>{@code true} (default) → {@link SearchEngineScanBackend}: full project
     * scan, correct cross-file semantics, cost proportional to project size.</li>
     * <li>{@code false} → {@link CompilationUnitScanBackend}: current-file only,
     * negligible cost, no cross-file detection.</li>
     * </ul>
     */
    public static volatile boolean USE_SEARCH_ENGINE = true;

    /**
     * Scans using the backend selected by {@link #USE_SEARCH_ENGINE}.
     *
     * @param project the Java project whose source scope is searched
     * @param unit the current compilation unit
     * @param extractor caller-supplied logic — decides what to collect from each type
     * @param monitor progress monitor
     * @return name → occurrence count across all types visited by the active backend
     * @throws CoreException on JDT or search errors
     */
    public static Map<String, Integer> scan(IJavaProject project,
                                            ICompilationUnit unit,
                                            NameExtractorStrategy extractor,
                                            IProgressMonitor monitor) throws CoreException {
        ScanBackend backend = USE_SEARCH_ENGINE ? SEARCH_ENGINE_BACKEND : COMPILATION_UNIT_BACKEND;
        LOGGER.info("[ProjectWideNameScanner] active backend: " + backend.getClass().getSimpleName());
        return scan(project, unit, extractor, backend, monitor);
    }

    /**
     * Scans using the explicitly supplied {@code backend}.
     *
     * <p>Use this overload to supply a custom backend (e.g. package-scoped,
     * cached, or incremental) without changing the global {@link #USE_SEARCH_ENGINE}
     * flag.
     *
     * @param project the Java project
     * @param unit the current compilation unit
     * @param extractor caller-supplied extraction logic
     * @param backend the scan backend to use
     * @param monitor progress monitor
     * @return name → occurrence count
     * @throws CoreException on JDT or search errors
     */
    public static Map<String, Integer> scan(IJavaProject project,
                                            ICompilationUnit unit,
                                            NameExtractorStrategy extractor,
                                            ScanBackend backend,
                                            IProgressMonitor monitor) throws CoreException {
        Map<String, Integer> nameCount = new HashMap<>();
        backend.visitTypes(project, unit, extractor, nameCount, monitor);
        return nameCount;
    }

    private ProjectWideNameScanner() {
        // utility class — no instances
    }
}
