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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures.FutureCancelChecker;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfo;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfoParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaProjectLabelsParams;
import org.eclipse.lsp4jakarta.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4jakarta.ls.api.JakartaJavaFileInfoProvider;
import org.eclipse.lsp4jakarta.ls.api.JakartaJavaProjectLabelsProvider;
import org.eclipse.lsp4jakarta.ls.commons.TextDocument;
import org.eclipse.lsp4jakarta.ls.commons.TextDocuments;
import org.eclipse.lsp4jakarta.ls.java.JakartaTextDocuments.JakartaTextDocument;
import org.eclipse.lsp4jakarta.version.JakartaVersion;

/**
 * Java Text documents registry which manages opened Java file.
 *
 * Based on:
 * https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/ls/java/JavaTextDocuments.java
 *
 * @author Angelo ZERR
 *
 */
public class JakartaTextDocuments extends TextDocuments<JakartaTextDocument> {

    private static final String JAKARTA_PROJECT_LABEL = "jakarta";

    private static final Logger LOGGER = Logger.getLogger(JakartaTextDocuments.class.getName());

    private static final ProjectLabelInfoEntry PROJECT_INFO_LOADING = new ProjectLabelInfoEntry(null, null, null, null);

    private final Map<String /* Java file URI */, CompletableFuture<ProjectLabelInfoEntry>> documentCache;

    private final Map<String /* project URI */, CompletableFuture<ProjectLabelInfoEntry>> projectCache;

    private final JakartaJavaProjectLabelsProvider projectInfoProvider;

    private final JakartaJavaFileInfoProvider fileInfoProvider;

    private JavaTextDocumentSnippetRegistry snippetRegistry;

    private boolean hasLoadedAllProjects = false;

    /**
     * Opened Java file.
     *
     */
    public class JakartaTextDocument extends TextDocument {

        private String projectURI;

        private String packageName;

        private CompletableFuture<JakartaJavaFileInfo> fileInfoFuture;

        public JakartaTextDocument(TextDocumentItem document) {
            super(document);
            collectFileInfo();
        }

        /**
         * Collect Java file information (ex : package name) from the JDT LS side.
         */
        private void collectFileInfo() {
            if (fileInfoProvider != null) {
                if (fileInfoFuture == null || fileInfoFuture.isCancelled()
                    || fileInfoFuture.isCompletedExceptionally()) {
                    JakartaJavaFileInfoParams params = new JakartaJavaFileInfoParams();
                    params.setUri(super.getUri());
                    fileInfoFuture = fileInfoProvider.getJavaFileInfo(params);
                }
                JakartaJavaFileInfo fileInfo = fileInfoFuture.getNow(null);
                if (fileInfo != null) {
                    setPackageName(fileInfo.getPackageName());
                }
            }
        }

        public String getProjectURI() {
            return projectURI;
        }

        public String getPackageName() {
            if (packageName == null) {
                collectFileInfo();
            }
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public void setProjectURI(String projectURI) {
            this.projectURI = projectURI;
        }

        /**
         * Execute the given code only if the Java file belongs to a Jakarta project
         * without waiting for the load of project information.
         *
         * @param <T> the type to return.
         * @param code the code to execute.
         * @param defaultValue the default value to return if the Java file doesn't
         *            belong to a Jakarta project.
         * @return the given code only if the Java file belongs to a Jakarta
         *         project without waiting for the load of project information.
         */
        public <T> CompletableFuture<T> executeIfInJakartaProject(
                                                                  BiFunction<ProjectLabelInfoEntry, CancelChecker, CompletableFuture<T>> code, T defaultValue) {
            return executeIfInJakartaProject(code, defaultValue, false);
        }

        /**
         * Execute the given code only if the Java file belongs to a Jakarta
         * project.
         *
         * @param <T> the type to return.
         * @param code the code to execute.
         * @param defaultValue the default value to return if the Java file
         *            doesn't belong to a Jakarta project.
         * @param waitForLoadingProjectInfo true if code to apply must be done when
         *            project information is loaded and false
         *            otherwise.
         * @return the given code only if the Java file belongs to a Jakarta
         *         project.
         */
        public <T> CompletableFuture<T> executeIfInJakartaProject(
                                                                  BiFunction<ProjectLabelInfoEntry, CancelChecker, CompletableFuture<T>> code, T defaultValue,
                                                                  boolean waitForLoadingProjectInfo) {
            return computeAsyncCompose(cancelChecker -> {
                CompletableFuture<ProjectLabelInfoEntry> projectInfoFuture = getProjectInfo(this);
                ProjectLabelInfoEntry projectInfo = projectInfoFuture.getNow(PROJECT_INFO_LOADING);
                if (isProjectInfoLoading(projectInfo)) {
                    // The project information is loading.
                    if (!waitForLoadingProjectInfo) {
                        // don't wait the load of the project, apply the given code.
                        return executeIfInJakartaProject(null, code, defaultValue, cancelChecker);
                    }
                    // Wait the load of the project and apply the given code.
                    return projectInfoFuture.thenCompose(loadedProjectInfo -> {
                        return executeIfInJakartaProject(loadedProjectInfo, code, defaultValue, cancelChecker);
                    });
                }
                // The project information is loaded, apply the given code
                return executeIfInJakartaProject(projectInfo, code, defaultValue, cancelChecker);
            });
        }

        private <T> CompletableFuture<T> executeIfInJakartaProject(ProjectLabelInfoEntry projectInfo,
                                                                   BiFunction<ProjectLabelInfoEntry, CancelChecker, CompletableFuture<T>> code, T defaultValue,
                                                                   CancelChecker cancelChecker) {
            cancelChecker.checkCanceled();
            if (projectInfo == null || !isJakartaProject(projectInfo)) {
                return CompletableFuture.completedFuture(defaultValue);
            }
            return code.apply(projectInfo, cancelChecker);
        }

        /**
         * Returns true if the Java file belongs to a Jakarta project and false
         * otherwise.
         *
         * @return true if the Java file belongs to a Jakarta project and false
         *         otherwise.
         */
        public boolean isInJakartaProject() {
            ProjectLabelInfoEntry projectInfo = getProjectInfo(this).getNow(null);
            return isJakartaProject(projectInfo);
        }
    }

    public JakartaTextDocuments(JakartaJavaProjectLabelsProvider projectInfoProvider,
                                JakartaJavaFileInfoProvider fileInfoProvider) {
        this.projectInfoProvider = projectInfoProvider;
        this.fileInfoProvider = fileInfoProvider;
        this.documentCache = new ConcurrentHashMap<>();
        this.projectCache = new ConcurrentHashMap<>();
    }

    @Override
    public JakartaTextDocument createDocument(TextDocumentItem document) {
        JakartaTextDocument doc = new JakartaTextDocument(document);
        doc.setIncremental(isIncremental());
        return doc;
    }

    /**
     * Returns as promise the Jakarta project information for the given java
     * file document.
     *
     * @param document the java file document.
     * @return as promise the Jakarta project information for the given java
     *         file document.
     */
    private CompletableFuture<ProjectLabelInfoEntry> getProjectInfo(JakartaTextDocument document) {
        //TODO: revert this to getProjectInfoFromCache when/if ProjectLabelInfoEntry caching is re-enabled
        return getProjectInfoFromClient(document). //
                        exceptionally(ex -> {
                            LOGGER.log(Level.WARNING, String.format(
                                                                    "Error while getting ProjectLabelInfoEntry (classpath) for '%s'", document.getUri()),
                                       ex);
                            return null;
                        });
    }

    CompletableFuture<ProjectLabelInfoEntry> getProjectInfoFromClient(JakartaTextDocument document) {
        String documentURI = document.getUri();
        // Search future which load project info
        CompletableFuture<ProjectLabelInfoEntry> projectInfo = null;

        // TODO: re-enable the retrieval of ProjectLabelInfoEntry objects from the caches?

        // We are not going to cache the ProjectLabelInfoEntry for right now - currently caching
        // is breaking the classpath filtering behaviour - if the pom changes to restrict the classpath, caching
        // prevents us from returning to the client to re-compute the classpath contents.

        // see getProjectInfoFromCache

        JakartaJavaProjectLabelsParams params = new JakartaJavaProjectLabelsParams();
        params.setUri(documentURI);
        params.setTypes(getSnippetRegistry().getTypes());
        final CompletableFuture<ProjectLabelInfoEntry> future = projectInfoProvider.getJavaProjectLabels(params);
        ProjectLabelInfoEntry entry = null;
        try {
            // future.get() will definitely wait but forced me to re-write the code abit
            entry = future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.WARNING, String.format(
                                                    "Error while getting the CompletableFuture ProjectLabelInfoEntry object from client for '%s'", document.getUri()),
                       e);
        }

        // TODO: re-enable caching of the ProjectLabelInfoEntry objects?

        // since we have effectively disabled cache lookups, and go to the client on every completion call
        // (as we were doing in the previous code base) we will also disable the addition of
        // a ProjectLabelInfoEntry into the caches

        // see getProjectInfoFromCache

        return future;

    }

    CompletableFuture<ProjectLabelInfoEntry> getProjectInfoFromCache(JakartaTextDocument document) {
        String projectURI = document.getProjectURI();
        String documentURI = document.getUri();
        // Search future which load project info in cache
        CompletableFuture<ProjectLabelInfoEntry> projectInfo = null;

        if (projectURI != null) {
            // the java document has already been linked to a project URI, get future from
            // the project cache.
            projectInfo = projectCache.get(projectURI);
        } else {
            // get the current future for the given document URI
            projectInfo = documentCache.get(documentURI);
        }
        if (projectInfo == null || projectInfo.isCancelled() || projectInfo.isCompletedExceptionally()) {
            // not found in the cache, load the project info from the JDT LS Extension
            JakartaJavaProjectLabelsParams params = new JakartaJavaProjectLabelsParams();
            params.setUri(documentURI);
            params.setTypes(getSnippetRegistry().getTypes());
            final CompletableFuture<ProjectLabelInfoEntry> future = projectInfoProvider.getJavaProjectLabels(params);
            // >> changed this section starting here
            ProjectLabelInfoEntry entry = null;
            try {
                // future.get() will definitely wait but forced me to re-write the code abit
                entry = future.get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (entry != null) {
                // project info with labels are get from the JDT LS
                String newProjectURI = entry.getUri();
                // cache the project info in the project cache level.
                projectCache.put(newProjectURI, future);
                // update the project URI of the document to link it to a project URI
                document.setProjectURI(newProjectURI);
                // evict the document cache level.
                documentCache.remove(documentURI);
            }
            // cache the future in the document level.
            documentCache.put(documentURI, future);
            return future;
        }

        // Returns the cached project info
        return projectInfo;
    }

    /**
     * Returns a list of all projects in the current workspace as a completable
     * future
     *
     * Loads and caches any projects that have not yet been loaded into the cache.
     *
     * @returns a list of all projects in the current workspace as a completable
     *          future
     */
    public CompletableFuture<List<ProjectLabelInfoEntry>> getWorkspaceProjects() {
        if (!hasLoadedAllProjects) {
            return projectInfoProvider.getAllJavaProjectLabels() //
                            .thenApply(entries -> {
                                if (entries != null && entries.size() > 0) {
                                    for (ProjectLabelInfoEntry entry : entries) {
                                        if (entry != null) {
                                            String newProjectURI = entry.getUri();
                                            if (!projectCache.containsKey(newProjectURI)) {
                                                projectCache.put(newProjectURI, CompletableFuture.completedFuture(entry));
                                            }
                                        }
                                    }
                                }
                                hasLoadedAllProjects = true;
                                return entries;
                            });
        }

        // otherwise the list of all projects is cached
        Collection<CompletableFuture<ProjectLabelInfoEntry>> projectLabelFutures = projectCache.values();
        return CompletableFuture.allOf((CompletableFuture[]) projectLabelFutures.stream().toArray(CompletableFuture[]::new)) //
                        .thenApply(voidFuture -> {
                            return projectLabelFutures.stream() //
                                            .map(futureProject -> {
                                                return futureProject.getNow(null);
                                            }) //
                                            .filter(project -> project != null) //
                                            .distinct() //
                                            .collect(Collectors.toList());
                        });
    }

    /**
     * Returns true if the given project information has the "jakarta" label
     * and false otherwise.
     *
     * @param projectInfo the project information.
     * @return true if the given project information has the "jakarta" label
     *         and false otherwise.
     */
    private static boolean isJakartaProject(ProjectLabelInfoEntry projectInfo) {
        return projectInfo != null && projectInfo.hasLabel(JAKARTA_PROJECT_LABEL);
    }

    public JavaTextDocumentSnippetRegistry getSnippetRegistry() {
        if (snippetRegistry == null) {
            snippetRegistry = new JavaTextDocumentSnippetRegistry();
        }
        return snippetRegistry;
    }

    private static <R> CompletableFuture<R> computeAsyncCompose(Function<CancelChecker, CompletableFuture<R>> code) {
        CompletableFuture<CancelChecker> start = new CompletableFuture<>();
        CompletableFuture<R> result = start.thenComposeAsync(code);
        start.complete(new FutureCancelChecker(result));
        return result;
    }

    private static boolean isProjectInfoLoading(ProjectLabelInfoEntry projectInfo) {
        return PROJECT_INFO_LOADING == projectInfo;
    }
}
