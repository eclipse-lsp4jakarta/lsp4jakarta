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
package org.eclipse.lsp4jakarta.ls.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4jakarta.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4jakarta.commons.utils.StringUtils;
import org.eclipse.lsp4jakarta.ls.commons.BadLocationException;
import org.eclipse.lsp4jakarta.ls.commons.snippets.ISnippetContext;
import org.eclipse.lsp4jakarta.ls.commons.snippets.Snippet;
import org.eclipse.lsp4jakarta.ls.commons.snippets.TextDocumentSnippetRegistry;
import org.eclipse.lsp4jakarta.ls.java.JakartaTextDocuments.JakartaTextDocument;
import org.eclipse.lsp4jakarta.snippets.LanguageId;
import org.eclipse.lsp4jakarta.snippets.SnippetContextForJava;

/**
 * Java snippet registry. When a snippet is registered it replaces for the first
 * line only the content 'package ${1:packagename};' to '${packagename}' in
 * order to manage the case if packagename is empty (don't generate package) or
 * not.
 *
 * Based on:
 * https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/ls/java/JavaTextDocumentSnippetRegistry.java
 *
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentSnippetRegistry extends TextDocumentSnippetRegistry {

    private static final String PACKAGENAME_KEY = "packagename";
    private static final String EE_NAMESPACE_KEY = "ee-namespace";
    private static final String JAVAX_VALUE = "javax";
    private static final String JAKARTA_VALUE = "jakarta";

    /**
     * The type whose presence indicates that the jakarta namespace should be used.
     */
    private static final String JAKARTA_FLAG_TYPE = "jakarta.ws.rs.GET";

    private List<String> types;

    public JavaTextDocumentSnippetRegistry() {
        this(true);
    }

    public JavaTextDocumentSnippetRegistry(boolean loadDefault) {
        super(LanguageId.java.name(), loadDefault);
    }

    /**
     * Returns the all distinct types declared in context/type of each snippet.
     *
     * @return the all distinct types declared in context/type of each snippet.
     */
    public List<String> getTypes() {
        if (types != null) {
            return types;
        }
        types = collectTypes();
        return types;
    }

    private synchronized List<String> collectTypes() {
        if (types != null) {
            return types;
        }
        List<String> types = new ArrayList<>();
        types.add(JAKARTA_FLAG_TYPE);
        for (Snippet snippet : getSnippets()) {
            if (snippet.getContext() != null && snippet.getContext() instanceof SnippetContextForJava) {
                List<String> snippetTypes = ((SnippetContextForJava) snippet.getContext()).getTypes();
                if (snippetTypes != null) {
                    for (String snippetType : snippetTypes) {
                        if (!types.contains(snippetType)) {
                            types.add(snippetType);
                        }
                    }
                }
            }
        }
        return types;
    }

    @Override
    public void registerSnippet(Snippet snippet) {
        preprocessSnippetBody(snippet);
        super.registerSnippet(snippet);
    }

    /**
     * Preprocess Snippet body for managing package name.
     *
     * @param snippet
     */
    private void preprocessSnippetBody(Snippet snippet) {
        List<String> body = snippet.getBody();
        if (body.isEmpty()) {
            return;
        }
        String firstLine = body.get(0);
        if (firstLine.contains("${") && firstLine.contains(PACKAGENAME_KEY)) {
            // Transform these 3 body lines:
            // "package ${1:packagename};",
            // "",
            // "import jakarta.ws.rs.HEAD;",

            // to one line:
            // ${packagename}import jakarta.ws.rs.HEAD;

            if (body.size() >= 2 && StringUtils.isEmpty(body.get(1))) {
                // Remove the line ""
                body.remove(1);
            }
            String line = "";
            if (body.size() >= 2) {
                // Remove the line "import jakarta.ws.rs.HEAD;"
                line = body.get(1);
                body.remove(1);
            }
            // Update the line 0 to ${packagename}import
            // jakarta.ws.rs.HEAD;
            body.set(0, "${" + PACKAGENAME_KEY + "}" + line);
        }
    }

    public List<CompletionItem> getCompletionItems(JakartaTextDocument document, int completionOffset,
                                                   boolean canSupportMarkdown, boolean snippetsSupported,
                                                   BiPredicate<ISnippetContext<?>, Map<String, String>> contextFilter,
                                                   ProjectLabelInfoEntry projectInfo) {
        return getCompletionItems(document, completionOffset, canSupportMarkdown, snippetsSupported,
                                  contextFilter, projectInfo, null);
    }

    public List<CompletionItem> getCompletionItems(JakartaTextDocument document, int completionOffset,
                                                   boolean canSupportMarkdown, boolean snippetsSupported,
                                                   BiPredicate<ISnippetContext<?>, Map<String, String>> contextFilter,
                                                   ProjectLabelInfoEntry projectInfo, String selectedVersion) {
        Map<String, String> model = new HashMap<>();
        String packageStatement = "";
        String packageName = document.getPackageName();
        String lineDelimiter = System.lineSeparator();
        try {
            lineDelimiter = document.lineDelimiter(0);
        } catch (BadLocationException e) {
        }
        if (packageName == null) {
            packageStatement = new StringBuilder("package ${1:packagename};")//
                            .append(lineDelimiter) //
                            .append(lineDelimiter) //
                            .toString();
        } else {
            // fill package name to replace in the snippets
            if (packageName.length() > 0) {
                packageStatement = new StringBuilder("package ")//
                                .append(document.getPackageName()) //
                                .append(";") //
                                .append(lineDelimiter) //
                                .append(lineDelimiter) //
                                .toString();
            }
        }
        model.put(PACKAGENAME_KEY, packageStatement);
        model.put(EE_NAMESPACE_KEY,
                  projectInfo.getLabels().contains(JavaTextDocumentSnippetRegistry.JAKARTA_FLAG_TYPE) ? JavaTextDocumentSnippetRegistry.JAKARTA_VALUE : JavaTextDocumentSnippetRegistry.JAVAX_VALUE);

        List<CompletionItem> items = super.getCompletionItems(document, completionOffset, canSupportMarkdown, snippetsSupported,
                                                              contextFilter, model);

        return filterItemsByVersion(items, selectedVersion);
    }

    /**
     * Removes the completion items of snippets that do not support the selected
     * Jakarta EE version.
     * <p>
     * Each item is matched back to its snippet through its filterText (the first
     * prefix of the snippet). Items that cannot be matched to a snippet are kept.
     * </p>
     *
     * @param items the completion items to filter
     * @param selectedVersion the selected version level (for example "10"), or null to keep all items
     * @return the items whose snippet supports the selected version
     */
    private List<CompletionItem> filterItemsByVersion(List<CompletionItem> items, String selectedVersion) {
        if (selectedVersion == null) {
            return items;
        }

        Map<String, Snippet> snippetByPrefix = getSnippets().stream().filter(s -> s.getPrefixes() != null
                                                                                  && !s.getPrefixes().isEmpty()).collect(Collectors.toMap(s -> s.getPrefixes().get(0), s -> s,
                                                                                                                                          (a, b) -> a));

        return items.stream().filter(item -> {
            Snippet snippet = snippetByPrefix.get(item.getFilterText());
            return snippet == null || snippet.supportsVersion(selectedVersion);
        }).collect(Collectors.toList());
    }

}
