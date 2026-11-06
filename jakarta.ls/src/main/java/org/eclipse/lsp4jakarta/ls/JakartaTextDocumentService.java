/*******************************************************************************
* Copyright (c) 2020, 2023 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.ls;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4jakarta.commons.DocumentFormat;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCompletionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCompletionResult;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsSettings;
import org.eclipse.lsp4jakarta.commons.JakartaJavaProjectLabelsParams;
import org.eclipse.lsp4jakarta.commons.JavaCursorContextResult;
import org.eclipse.lsp4jakarta.ls.commons.BadLocationException;
import org.eclipse.lsp4jakarta.ls.commons.TextDocument;
import org.eclipse.lsp4jakarta.ls.commons.ValidatorDelayer;
import org.eclipse.lsp4jakarta.ls.commons.client.ExtendedClientCapabilities;
import org.eclipse.lsp4jakarta.ls.java.JakartaTextDocuments;
import org.eclipse.lsp4jakarta.ls.java.JakartaTextDocuments.JakartaTextDocument;
import org.eclipse.lsp4jakarta.ls.java.JavaTextDocumentSnippetRegistry;
import org.eclipse.lsp4jakarta.settings.JakartaTraceSettings;
import org.eclipse.lsp4jakarta.settings.SharedSettings;
import org.eclipse.lsp4jakarta.snippets.JavaSnippetCompletionContext;
import org.eclipse.lsp4jakarta.snippets.SnippetContextForJava;
import org.eclipse.lsp4jakarta.version.JakartaVersion;
import org.eclipse.lsp4jakarta.version.JakartaVersionFileStore;
import org.eclipse.lsp4jakarta.version.JakartaProjectVersionManager;
import org.eclipse.lsp4jakarta.version.VersionData;

public class JakartaTextDocumentService implements TextDocumentService {

    private static final Logger LOGGER = Logger.getLogger(JakartaTextDocumentService.class.getName());

    private final JakartaLanguageServer jakartaLanguageServer;
    private final SharedSettings sharedSettings;

    // Text document manager that maintains the contexts of the text documents
    private final JakartaTextDocuments documents;

    private ValidatorDelayer<JakartaTextDocument> validatorDelayer;

    // Jakarta EE version management
    private final JakartaProjectVersionManager versionManager;

    public JakartaTextDocumentService(JakartaLanguageServer jls, SharedSettings sharedSettings, JakartaTextDocuments jakartaTextDocuments) {
        this.jakartaLanguageServer = jls;
        this.sharedSettings = sharedSettings;
        this.documents = jakartaTextDocuments;
        this.versionManager = new JakartaProjectVersionManager(this::requestVersionSelection, this::isVersionSelectorSupported, this::revalidateProject);
        this.validatorDelayer = new ValidatorDelayer<>((javaTextDocument) -> {
            triggerValidationFor(javaTextDocument);
        });
    }

    private CompletableFuture<String> requestVersionSelection(Map<String, Object> params) {
        return jakartaLanguageServer.getLanguageClient().selectJakartaVersion(params);
    }

    private boolean isVersionSelectorSupported() {
        return jakartaLanguageServer.getCapabilityManager().getClientCapabilities().getExtendedClientCapabilities().isJakartaVersionSelector();
    }

    private void revalidateProject(String projectUri) {
        triggerValidationForAll(Set.of(projectUri));
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        documents.onDidCloseTextDocument(params);
        String uri = params.getTextDocument().getUri();
        // clear diagnostics
        jakartaLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {

        LOGGER.info("uri-----" + params.getTextDocument().getUri());
        //JakartaVersion v = JakartaVersionFinder.analyzeClasspath(params.getTextDocument().getUri());
        //System.out.println("Jakarta  version--------v-------" + v.getLabel());

        JakartaTextDocument document = documents.get(params.getTextDocument().getUri());

        return document.executeIfInJakartaProject((projectInfo, cancelChecker) -> {
            JakartaJavaCompletionParams javaParams = new JakartaJavaCompletionParams(params.getTextDocument().getUri(), params.getPosition());

            // get the completion capabilities from the java language server component
            CompletableFuture<JakartaJavaCompletionResult> javaParticipantCompletionsFuture = jakartaLanguageServer.getLanguageClient().getJavaCompletion(javaParams);

            // calculate params for Java snippets
            Integer completionOffset = null;
            try {
                completionOffset = document.offsetAt(params.getPosition());
            } catch (BadLocationException e) {
                // LOGGER.log(Level.SEVERE, "Error while getting java snippet completions", e);
                return null;
            }

            final Integer finalizedCompletionOffset = completionOffset;
            boolean canSupportMarkdown = true;
            boolean snippetsSupported = sharedSettings.getCompletionCapabilities().isCompletionSnippetsSupported();

            cancelChecker.checkCanceled();

            List<JakartaVersion> versions = projectInfo.getJakartaVersions();
            LOGGER.info("versions-----" + versions);

            return javaParticipantCompletionsFuture.thenApply((completionResult) -> {
                cancelChecker.checkCanceled();

                // We currently do not get any completion items from the JDT Extn layer - the
                // completion
                // list will be null, so we will new it up here to add the LS based snippets.
                // Will we in the future?
                CompletionList list = completionResult.getCompletionList();
                if (list == null) {
                    list = new CompletionList();
                }

                // We do get a cursorContext obj back from the JDT Extn layer - we will need
                // that for snippet selection
                JavaCursorContextResult cursorContext = completionResult.getCursorContext();

                // Get selected version for this project
                String selectedVersion = versionManager.getSelectedVersionLevel(projectInfo != null ? projectInfo.getUri() : null);

                // calculate the snippet completion items based on the cursor context
                JavaTextDocumentSnippetRegistry snippetRegistry = documents.getSnippetRegistry();
                List<CompletionItem> snippetCompletionItems = snippetRegistry.getCompletionItems(
                                                                                                 document, finalizedCompletionOffset, canSupportMarkdown,
                                                                                                 snippetsSupported,
                                                                                                 (context, model) -> {
                                                                                                     if (context != null
                                                                                                         && context instanceof SnippetContextForJava) {
                                                                                                         return ((SnippetContextForJava) context).isMatch(new JavaSnippetCompletionContext(projectInfo, cursorContext));
                                                                                                     }
                                                                                                     return true;
                                                                                                 }, projectInfo, selectedVersion);
                list.getItems().addAll(snippetCompletionItems);

                // This reduces the number of completion requests to the server. See:
                // https://microsoft.github.io/language-server-protocol/specifications/specification-current/#textDocument_completion
                list.setIsIncomplete(false);
                return Either.forRight(list);
            });

        }, Either.forLeft(Collections.emptyList()));
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        // Prepare the JakartaJavaCodeActionParams
        JakartaJavaCodeActionParams codeActionParams = new JakartaJavaCodeActionParams();
        codeActionParams.setTextDocument(params.getTextDocument());
        codeActionParams.setRange(params.getRange());
        codeActionParams.setContext(params.getContext());
        codeActionParams.setResourceOperationSupported(jakartaLanguageServer.getCapabilityManager().getClientCapabilities().isResourceOperationSupported());
        codeActionParams.setResolveSupported(jakartaLanguageServer.getCapabilityManager().getClientCapabilities().isCodeActionResolveSupported());

        // Pass the JakartaJavaCodeActionParams to IDE client, to be forwarded to the
        // JDT LS extension.
        return jakartaLanguageServer.getLanguageClient().getJavaCodeAction(codeActionParams) //
                        .thenApply(codeActions -> {
                            // Return the corresponding list of CodeActions, put in an Either and wrap as a
                            // CompletableFuture
                            return codeActions.stream().map(ca -> {
                                Either<Command, CodeAction> e = Either.forRight(ca);
                                return e;
                            }).collect(Collectors.toList());
                        });
    }

    @Override
    public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved) {
        return jakartaLanguageServer.getLanguageClient().resolveCodeAction(unresolved);
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        JakartaTextDocument document = documents.onDidOpenTextDocument(params);

        handleVersionSelectionAndValidate(document);
    }

    /**
     * Handles Jakarta EE version selection for a project and triggers validation.
     * This method ensures that:
     * 1. Only one version request is sent per project (even if multiple files open simultaneously)
     * 2. The didOpen call returns immediately without blocking
     * 3. All callbacks execute on a separate thread pool
     * 4. The selected version is persisted to a .jakarta-version file in the project directory
     * 5. Uses in-memory cache for fast access, with file as persistent storage
     * 6. Validates all opened files in the project after version selection
     *
     * @param document The document that was opened
     */
    private void handleVersionSelectionAndValidate(JakartaTextDocument document) {
        document.executeIfInJakartaProject((projectInfo, cancelChecker) -> {
            // Get the project URI from projectInfo - this is the project-level identifier
            String projectUri = projectInfo.getUri();
            List<JakartaVersion> detectedVersions = projectInfo.getJakartaVersions();
            LOGGER.info("versions loaded from backend-----" + detectedVersions);
            if (projectUri == null) {
                // Project URI not available, skip version selection and run diagnostics directly
                triggerValidationFor(Arrays.asList(document.getUri()), null);
                return null;
            }

            // Version already known (cache or file): validate this document only.
            // Otherwise the manager resolves it asynchronously and revalidates all open files.
            if (versionManager.resolve(projectUri, detectedVersions)) {
                triggerValidationFor(Arrays.asList(document.getUri()), projectUri);
            }
            return null;
        }, null, true);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        validate(documents.onDidChangeTextDocument(params), true);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // validate all opened java files which belong to a Jakarta project
        triggerValidationForAll(null);
    }

    private void validate(JakartaTextDocument javaTextDocument, boolean delay) {
        if (delay) {
            validatorDelayer.validateWithDelay(javaTextDocument);
        } else {
            triggerValidationFor(javaTextDocument);
        }
    }

    /**
     * Validate all opened Java files which belong to a Jakarta project.
     *
     * @param projectURIs list of project URIs filter and null otherwise.
     */
    private void triggerValidationForAll(Set<String> projectURIs) {
        String projectUri = (projectURIs != null && !projectURIs.isEmpty()) ? projectURIs.iterator().next() : null;

        List<String> uris = documents.all().stream().map(TextDocument::getUri).collect(Collectors.toList());

        triggerValidationFor(uris, projectUri);
    }

    /**
     * Validate the given opened Java file.
     *
     * @param document the opened Java file.
     */
    private void triggerValidationFor(JakartaTextDocument document) {
        document.executeIfInJakartaProject((projectinfo, cancelChecker) -> {
            String uri = document.getUri();
            String projectUri = projectinfo != null ? projectinfo.getUri() : null;
            triggerValidationFor(Arrays.asList(uri), projectUri);
            return null;
        }, null, true);
    }

    /**
     * Validate all given Java files uris.
     *
     * @param uris Java files uris to validate.
     * @param projectUri The project URI for version information lookup (can be null).
     */
    private void triggerValidationFor(List<String> uris, String projectUri) {
        if (uris.isEmpty()) {
            return;
        }

        // Create diagnostics settings and populate with version information
        JakartaJavaDiagnosticsSettings settings = new JakartaJavaDiagnosticsSettings(null);

        // Get version information from the project URI
        VersionData versionData = versionManager.getVersionData(projectUri);
        if (versionData != null) {
            settings.setSelectedVersion(versionData.getVersion());
            settings.setAvailableVersions(versionData.getAvailableVersions());
        }

        JakartaJavaDiagnosticsParams javaParams = new JakartaJavaDiagnosticsParams(uris, settings);

        boolean markdownSupported = sharedSettings.getHoverSettings().isContentFormatSupported(MarkupKind.MARKDOWN);
        if (markdownSupported) {
            javaParams.setDocumentFormat(DocumentFormat.Markdown);
        }

        jakartaLanguageServer.getLanguageClient().getJavaDiagnostics(javaParams).thenApply(diagnostics -> {
            if (diagnostics == null) {
                return null;
            }
            for (PublishDiagnosticsParams diagnostic : diagnostics) {
                jakartaLanguageServer.getLanguageClient().publishDiagnostics(diagnostic);
            }
            return null;
        });
    }

    protected void cleanDiagnostics() {
        // clear existing diagnostics
        documents.all().forEach(doc -> {
            jakartaLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(doc.getUri(), new ArrayList<Diagnostic>()));
        });
    }

    /**
     * Clears the in-memory version cache for all projects.
     * Called during server shutdown before full cleanup.
     */
    public void clearVersionCache() {
        versionManager.clearCache();
    }

    /**
     * Shutdown the text document service and clean up resources.
     * Note: Version files on disk are preserved for persistence across server restarts.
     */
    public void shutdown() {
        versionManager.shutdown();
    }

    /**
     * Update shared settings from the client capabilities.
     *
     * @param capabilities the client capabilities
     * @param extendedClientCapabilities the extended client capabilities
     */
    public void updateClientCapabilities(ClientCapabilities capabilities,
                                         ExtendedClientCapabilities extendedClientCapabilities) {
        TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
        if (textDocumentClientCapabilities != null) {
            sharedSettings.getCompletionCapabilities().setCapabilities(textDocumentClientCapabilities.getCompletion());
            sharedSettings.getHoverSettings().setCapabilities(textDocumentClientCapabilities.getHover());
        }
    }

    /**
     * Updates the trace settings defined by the client flowing requests between the LS and JDT extensions.
     *
     * @param newTrace The new trace setting.
     */
    public void updateTraceSettings(JakartaTraceSettings newTrace) {
        JakartaTraceSettings trace = sharedSettings.getTraceSettings();
        trace.update(newTrace);
    }

    /**
     * Resets the Jakarta EE version for a project and triggers re-selection and re-validation.
     * This method:
     * 1. Clears the version from memory cache
     * 2. Deletes the version file from disk
     * 3. Prompts user to select a new version
     * 4. Re-validates all opened Jakarta files after selection
     *
     * @param projectUri The project URI to reset version for
     */
    public void resetVersionAndRevalidate(String projectUri) {
        if (projectUri == null || projectUri.isEmpty()) {
            LOGGER.warning("Cannot reset version: project URI is null or empty");
            return;
        }

        versionManager.clear(projectUri);

        // getJavaProjectLabels needs a file URI (not a project URI) — findProject calls
        // utils.findFile(uri) which only resolves files, not directories.
        // Use the first open file that belongs to this project.
        // Document URIs are file URIs while projectUri is a plain OS path (e.g. C:\proj or /home/proj),
        // so compare them as paths to work on Windows, Linux and macOS.
        Path projectPath = JakartaVersionFileStore.toPath(projectUri);
        String fileUri = projectPath == null ? null : documents.all().stream().map(TextDocument::getUri).filter(u -> {
            Path documentPath = JakartaVersionFileStore.toPath(u);
            return documentPath != null && documentPath.startsWith(projectPath);
        }).findFirst().orElse(null);

        if (fileUri == null) {
            LOGGER.warning("No open file found for project: " + projectUri + ", cannot reset version");
            return;
        }

        JakartaJavaProjectLabelsParams labelsParams = new JakartaJavaProjectLabelsParams();
        labelsParams.setUri(fileUri);
        jakartaLanguageServer.getJavaProjectLabels(labelsParams).thenAcceptAsync(projectInfo -> {
            if (projectInfo == null) {
                LOGGER.warning("No project info found for file: " + fileUri);
                return;
            }
            if (projectInfo.getJakartaVersions().isEmpty()) {
                LOGGER.warning("No versions detected for project: " + projectUri);
                return;
            }
            versionManager.resolveAfterReset(projectUri, projectInfo.getJakartaVersions());
        }, versionManager.getExecutor());
    }
}
