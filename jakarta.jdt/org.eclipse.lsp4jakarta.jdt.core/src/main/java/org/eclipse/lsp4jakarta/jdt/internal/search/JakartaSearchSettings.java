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

/**
 * Feature-level on/off switch for all diagnostics that require a
 * project-wide search-engine scan.
 *
 * <h3>Control model</h3>
 *
 * <pre>
 *  SEARCH_ENGINE_DIAGNOSTICS_ENABLED   (this class)
 *    false → diagnostic returns empty immediately; no scan, no validation
 *    true  → ProjectWideNameScanner.scan() runs using SearchEngine
 * </pre>
 *
 * <h3>How to use in a new search-engine-based diagnostic</h3>
 *
 * <pre>{@code
 * public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, ...) {
 *     // ... null-check and file-level annotation guard ...
 *
 *     if (!JakartaSearchSettings.SEARCH_ENGINE_DIAGNOSTICS_ENABLED) {
 *         return diagnostics;  // feature disabled — skip scan entirely
 *     }
 *
 *     Map<String, Integer> counts = ProjectWideNameScanner.scan(...);
 *     // ... validate ...
 * }
 * }</pre>
 *
 * <h3>Turning the feature off</h3>
 * Set {@code SEARCH_ENGINE_DIAGNOSTICS_ENABLED = false} to disable all
 * search-engine-based diagnostics at once — e.g. in environments where the
 * full project index is unavailable or during tests that do not expect
 * cross-file diagnostics.
 */
public final class JakartaSearchSettings {

    /**
     * Master switch for all search-engine-based diagnostics.
     *
     * <ul>
     * <li>{@code true} (default) — diagnostics that require a project-wide scan
     * are active; {@link ProjectWideNameScanner} runs using
     * {@link SearchEngine}.</li>
     * <li>{@code false} — every diagnostic that checks this flag returns an
     * empty result immediately, with no scan and no validation.</li>
     * </ul>
     */
    public static volatile boolean SEARCH_ENGINE_DIAGNOSTICS_ENABLED = true;

    private JakartaSearchSettings() {
        // settings class — no instances
    }
}
