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

/**
 * {@link ScanBackend} that visits only the types declared in the current
 * compilation unit.
 *
 * <p>O(1) types visited per diagnostic call — negligible cost regardless of
 * project size. Cannot detect names declared in other files; use this as a
 * fast performance baseline or when cross-file detection is intentionally
 * disabled.
 *
 * <p>Toggle between this backend and {@link SearchEngineScanBackend} via
 * {@link ProjectWideNameScanner#USE_SEARCH_ENGINE}.
 */
public class CompilationUnitScanBackend implements ScanBackend {

    private static final Logger LOGGER = Logger.getLogger(CompilationUnitScanBackend.class.getName());

    @Override
    public void visitTypes(IJavaProject project,
                           ICompilationUnit unit,
                           NameExtractorStrategy extractor,
                           Map<String, Integer> nameCount,
                           IProgressMonitor monitor) throws CoreException {
        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            extractor.extract(type, nameCount);
        }
        LOGGER.info(String.format(
                                  "[ProjectWideNameScanner][CompilationUnit] types_visited=%d | unique_names=%d",
                                  types.length, nameCount.size()));
    }
}
