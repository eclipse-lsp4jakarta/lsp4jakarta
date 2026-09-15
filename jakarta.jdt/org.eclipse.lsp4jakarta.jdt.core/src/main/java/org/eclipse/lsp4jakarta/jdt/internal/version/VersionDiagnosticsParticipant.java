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
package org.eclipse.lsp4jakarta.jdt.internal.version;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Jakarta version diagnostics participant.
 *
 * This participant adds a persistent diagnostic at the first line of every Jakarta file,
 * allowing users to update or configure the Jakarta EE version for the project.
 */
public class VersionDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        List<Diagnostic> diagnostics = new ArrayList<>();

        // Get version information from settings
        String selectedVersion = context.getSettings().getSelectedVersion();
        List<String> availableVersions = context.getSettings().getAvailableVersions();

        // Only show diagnostic if there are 2 or more available versions
        if (availableVersions == null || availableVersions.size() <= 1) {
            return diagnostics;
        }

        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);

        if (unit == null) {
            return diagnostics;
        }

        // Calculate range from first char (0) to last char of first line
        int firstLineLength = getFirstLineLength(unit);
        Range range = context.getUtils().toRange(unit, 0, firstLineLength);

        // Create diagnostic message showing current version
        String message = selectedVersion != null ? "Multiple Jakarta EE versions are present in this project. The current active version is "
                                                   + selectedVersion
                                                   + ".\nRecommend to use single version to avoid conflicts. " : "Multiple Jakarta EE versions are present in this project.";

        Diagnostic versionDiagnostic = context.createDiagnostic(
                                                                uri,
                                                                message,
                                                                range,
                                                                Constants.DIAGNOSTIC_SOURCE,
                                                                ErrorCode.MultipleJakartaEEVersions,
                                                                DiagnosticSeverity.Warning);

        diagnostics.add(versionDiagnostic);

        return diagnostics;
    }

    /**
     * Gets the length of the first line in the compilation unit.
     * Returns at least 1 to ensure a valid range even for empty first lines.
     *
     * @param unit the compilation unit
     * @return the length of the first line (minimum 1)
     */
    private int getFirstLineLength(ICompilationUnit unit) {
        try {
            String source = unit.getSource();
            if (source == null || source.isEmpty()) {
                return 1;
            }

            // Find the first newline character
            int newlineIndex = source.indexOf('\n');
            if (newlineIndex == -1) {
                // No newline found, return entire source length (at least 1)
                return Math.max(1, source.length());
            }

            // Return length up to (but not including) the newline
            // If first line is empty (newlineIndex == 0), return 1 to include the newline
            return Math.max(1, newlineIndex);
        } catch (JavaModelException e) {
            return 1;
        }
    }

}
