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

package org.eclipse.lsp4jakarta.ls.commons.snippets;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Snippet description (like vscode snippet).
 *
 * @author Angelo ZERR
 *
 */
public class Snippet {

    private String label;

    private List<String> prefixes;

    private String suffix;

    private List<String> body;

    private String description;

    private String scope;

    private String sortText;

    private ISnippetContext<?> context;

    private List<String> versions;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List<String> getBody() {
        return body;
    }

    public void setBody(List<String> body) {
        this.body = body;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSortText() {
        return sortText;
    }

    public void setSortText(String sortText) {
        this.sortText = sortText;
    }

    public ISnippetContext<?> getContext() {
        return context;
    }

    public void setContext(ISnippetContext<?> context) {
        this.context = context;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public boolean hasContext() {
        return getContext() != null;
    }

    public boolean match(BiPredicate<ISnippetContext<?>, Map<String, String>> contextFilter,
                         Map<String, String> model) {
        if (!hasContext()) {
            return true;
        }
        return contextFilter.test(getContext(), model);
    }

    /**
     * Checks if the given version is supported by this snippet.
     *
     * @param version the version to check
     * @return true if the snippet supports the given version, false otherwise
     */
    public boolean supportsVersion(String version) {
        if (versions == null || versions.isEmpty()) {
            return true; // no version restriction, support all versions
        }
        return versions.contains(version);
    }

}