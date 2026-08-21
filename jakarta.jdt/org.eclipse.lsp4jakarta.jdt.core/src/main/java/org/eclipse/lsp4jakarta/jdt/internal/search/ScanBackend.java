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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

/**
 * Pluggable backend that controls <em>which</em> {@link IType types} are visited
 * during a {@link ProjectWideNameScanner} scan.
 *
 * <p>Implementations differ only in which types they deliver to the
 * {@link NameExtractorStrategy}; they do not interpret the types themselves.
 * Two built-in implementations are provided:
 * <ul>
 * <li>{@link SearchEngineScanBackend} — visits every source type in the project.</li>
 * <li>{@link CompilationUnitScanBackend} — visits only types in the current file.</li>
 * </ul>
 *
 * <p>Custom backends (e.g. package-scoped, cached, or incremental) can be
 * supplied directly to
 * {@link ProjectWideNameScanner#scan(IJavaProject, ICompilationUnit, NameExtractorStrategy, ScanBackend, IProgressMonitor)}.
 */
public interface ScanBackend {

    /**
     * Visit the types determined by this backend and for each one call
     * {@code extractor.extract(type, nameCount)}.
     *
     * @param project the Java project
     * @param unit the current compilation unit (always available as fallback context)
     * @param extractor caller-supplied extraction logic — element-level agnostic
     * @param nameCount mutable accumulator to pass through to the extractor
     * @param monitor progress monitor
     * @throws CoreException on JDT or search errors
     */
    void visitTypes(IJavaProject project,
                    ICompilationUnit unit,
                    NameExtractorStrategy extractor,
                    Map<String, Integer> nameCount,
                    IProgressMonitor monitor) throws CoreException;
}
