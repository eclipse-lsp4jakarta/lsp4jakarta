/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.java.diagnostics;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.commons.DocumentFormat;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsSettings;
import org.eclipse.lsp4jakarta.jdt.core.java.AbstractJavaContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.version.JakartaDiagnostic;
import org.eclipse.lsp4jakarta.version.JakartaVersion;

/**
 * Java diagnostics context for a given compilation unit.
 *
 * @author Angelo ZERR
 *
 */
public class JavaDiagnosticsContext extends AbstractJavaContext {

    private final DocumentFormat documentFormat;

    private final JakartaJavaDiagnosticsSettings settings;

    public JavaDiagnosticsContext(String uri, ITypeRoot typeRoot, IJDTUtils utils, DocumentFormat documentFormat,
                                  JakartaJavaDiagnosticsSettings settings) {
        super(uri, typeRoot, utils);
        this.documentFormat = documentFormat;
        if (settings == null) {
            this.settings = new JakartaJavaDiagnosticsSettings(Collections.emptyList());
        } else {
            this.settings = settings;
        }
    }

    public DocumentFormat getDocumentFormat() {
        return documentFormat;
    }

    /**
     * Returns the JakartaJavaDiagnosticsSettings.
     *
     * Should not be null.
     *
     * @return the JakartaJavaDiagnosticsSettings
     */
    public JakartaJavaDiagnosticsSettings getSettings() {
        return this.settings;
    }

    public Diagnostic createDiagnostic(String uri, String message, Range range, String source, IJavaErrorCode code) {
        return createDiagnostic(uri, message, range, source, code, DiagnosticSeverity.Warning);
    }

    public Diagnostic createDiagnostic(String uri, String message, Range range, String source, IJavaErrorCode code,
                                       DiagnosticSeverity severity) {
        return createDiagnostic(uri, message, range, source, null, code, severity);

    }

    public Diagnostic createDiagnostic(String uri, String message, Range range, String source, Object data,
                                       IJavaErrorCode code,
                                       DiagnosticSeverity severity) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setSource(source);
        diagnostic.setMessage(message);
        diagnostic.setSeverity(severity);
        diagnostic.setRange(range);
        if (code != null) {
            diagnostic.setCode(code.getCode());
        }
        if (data != null) {
            diagnostic.setData(data);
        }
        return diagnostic;
    }

    /**
     * Removes the diagnostics that do not apply to the Jakarta EE version selected
     * for the project. Diagnostics are kept when no version is selected or when
     * their code is not registered in {@link JakartaDiagnostic}.
     *
     * @param diagnostics the diagnostics created by the participants
     * @return the diagnostics applicable to the selected version
     */
    public List<Diagnostic> filterByJakartaVersion(List<Diagnostic> diagnostics) {
        JakartaVersion version = JakartaVersion.fromLabel(settings.getSelectedVersion());
        if (version == JakartaVersion.UNKNOWN) {
            return diagnostics;
        }
        return diagnostics.stream().filter(diagnostic -> {
            JakartaDiagnostic jakartaDiagnostic = diagnostic.getCode() == null ? null : JakartaDiagnostic.getByCodeOrNull(diagnostic.getCode().getLeft());
            return jakartaDiagnostic == null || jakartaDiagnostic.isApplicableTo(version);
        }).collect(Collectors.toList());
    }

}
