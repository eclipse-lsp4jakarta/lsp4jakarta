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
package org.eclipse.lsp4jakarta.commons;

import java.util.List;

/**
 * Jakarta Java diagnostics parameters.
 *
 * Based on: https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/commons/MicroProfileJavaDiagnosticsParams.java
 *
 * @author Angelo ZERR
 */
public class JakartaJavaDiagnosticsParams {

    private List<String> uris;

    private int jakartaVersion;

    public int getJakartaVersion() {
        return jakartaVersion;
    }

    public void setJakartaVersion(int jakartaVersion) {
        this.jakartaVersion = jakartaVersion;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    private List<String> lines;

    private DocumentFormat documentFormat;

    private JakartaJavaDiagnosticsSettings settings;

    public JakartaJavaDiagnosticsParams() {
        this(null);
    }

    public JakartaJavaDiagnosticsParams(List<String> uris) {
        this(uris, null);
    }

    public JakartaJavaDiagnosticsParams(List<String> uris, JakartaJavaDiagnosticsSettings settings) {
        setUris(uris);
        this.settings = settings;
    }

    /**
     * Returns the java file uris list.
     *
     * @return the java file uris list.
     */
    public List<String> getUris() {
        return uris;
    }

    /**
     * Set the java file uris list.
     *
     * @param uris the java file uris list.
     */
    public void setUris(List<String> uris) {
        this.uris = uris;
    }

    public DocumentFormat getDocumentFormat() {
        return documentFormat;
    }

    public void setDocumentFormat(DocumentFormat documentFormat) {
        this.documentFormat = documentFormat;
    }

    /**
     * Returns the diagnostics settings.
     *
     * @return the diagnostics settings
     */
    public JakartaJavaDiagnosticsSettings getSettings() {
        return this.settings;
    }

    /**
     * Sets the diagnostics settings.
     *
     * @param settings the new value for the diagnostics settings
     */
    public void setSettings(JakartaJavaDiagnosticsSettings settings) {
        this.settings = settings;
    }

}