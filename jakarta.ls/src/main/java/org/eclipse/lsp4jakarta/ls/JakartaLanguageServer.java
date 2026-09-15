/*******************************************************************************
* Copyright (c) 2020, 2024 IBM Corporation and others.
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfo;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfoParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaProjectLabelsParams;
import org.eclipse.lsp4jakarta.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4jakarta.ls.api.JakartaJavaFileInfoProvider;
import org.eclipse.lsp4jakarta.ls.api.JakartaJavaProjectLabelsProvider;
import org.eclipse.lsp4jakarta.ls.api.JakartaLanguageClientAPI;
import org.eclipse.lsp4jakarta.ls.commons.ParentProcessWatcher.ProcessLanguageServer;
import org.eclipse.lsp4jakarta.ls.commons.client.ExtendedClientCapabilities;
import org.eclipse.lsp4jakarta.ls.commons.client.InitializationOptionsExtendedClientCapabilities;
import org.eclipse.lsp4jakarta.ls.java.JakartaTextDocuments;
import org.eclipse.lsp4jakarta.settings.AllJakartaSettings;
import org.eclipse.lsp4jakarta.settings.InitializationOptionsSettings;
import org.eclipse.lsp4jakarta.settings.JakartaGeneralClientSettings;
import org.eclipse.lsp4jakarta.settings.JakartaTraceSettings;
import org.eclipse.lsp4jakarta.settings.SharedSettings;
import org.eclipse.lsp4jakarta.settings.capabilities.JakartaCapabilityManager;
import org.eclipse.lsp4jakarta.settings.capabilities.ServerCapabilitiesInitializer;

/**
 * Jakarta Language server.
 */
public class JakartaLanguageServer implements LanguageServer, ProcessLanguageServer, JakartaJavaProjectLabelsProvider, JakartaJavaFileInfoProvider {

    private Integer parentProcessId;

    private static final Logger LOGGER = Logger.getLogger(JakartaLanguageServer.class.getName());

    private final WorkspaceService workspaceService;
    private final JakartaTextDocumentService textDocumentService;
    private final JakartaTextDocuments javaDocuments;
    private final SharedSettings sharedSettings;

    private JakartaLanguageClientAPI languageClient;
    private JakartaCapabilityManager capabilityManager;

    /**
     * Constructor
     */
    public JakartaLanguageServer() {
        workspaceService = new JakartaWorkspaceService(this);
        javaDocuments = new JakartaTextDocuments(this, this);
        sharedSettings = new SharedSettings();
        textDocumentService = new JakartaTextDocumentService(this, sharedSettings, javaDocuments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        LOGGER.info("Initializing Jakarta EE server using: " + System.getProperty("java.home"));
        this.parentProcessId = params.getProcessId();

        // Consume the capabilities supported by the client.
        ExtendedClientCapabilities extendedClientCapabilities = InitializationOptionsExtendedClientCapabilities.getExtendedClientCapabilities(params);
        capabilityManager.setClientCapabilities(params.getCapabilities(), extendedClientCapabilities);
        updateSettings(InitializationOptionsSettings.getSettings(params));
        textDocumentService.updateClientCapabilities(params.getCapabilities(), extendedClientCapabilities);

        // Send the capabilities supported by the sever to the client.
        ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer.getNonDynamicServerCapabilities(capabilityManager.getClientCapabilities());
        InitializeResult initializeResult = new InitializeResult(serverCapabilities);

        return CompletableFuture.completedFuture(initializeResult);
    }

    /**
     * Registers all capabilities that do not support client side preferences to
     * turn on/off
     *
     * (non-Javadoc)
     *
     * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.
     *      InitializedParams)
     */
    @Override
    public void initialized(InitializedParams params) {
        capabilityManager.initializeCapabilities();
    }

    /**
     * Update Jakarta settings configured by the client defined by the client flowing requests between
     * the LS and JDT extensions.
     *
     * @param initializationOptionsSettings the Jakarta settings
     */
    public synchronized void updateSettings(Object initializationOptionsSettings) {
        if (initializationOptionsSettings == null) {
            return;
        }

        initializationOptionsSettings = AllJakartaSettings.getJakartaToolsSettings(initializationOptionsSettings);
        JakartaGeneralClientSettings clientSettings = JakartaGeneralClientSettings.getGeneralJakartaSettings(initializationOptionsSettings);
        if (clientSettings != null) {
            JakartaTraceSettings newTrace = clientSettings.getTrace();
            if (newTrace != null) {
                textDocumentService.updateTraceSettings(newTrace);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Object> shutdown() {
        // Perform some clean up. During shutdown, TextDocumentService.didClose() may not be called properly.
        textDocumentService.cleanDiagnostics();
        textDocumentService.clearVersionCache();

        // If requested by the client, on shutdown (i.e. last file closed), shutdown the language server.
        if (capabilityManager.getClientCapabilities().shouldLanguageServerExitOnShutdown()) {
            LOGGER.info("Jakarta EE server is shutting down");
            ScheduledExecutorService delayer = Executors.newScheduledThreadPool(1);
            delayer.schedule(() -> exit(0), 1, TimeUnit.SECONDS);
        }

        return CompletableFutures.computeAsync(cc -> new Object());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        exit(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit(int exitCode) {
        System.exit(exitCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    public JakartaLanguageClientAPI getLanguageClient() {
        return this.languageClient;
    }

    public void setLanguageClient(LanguageClient languageClient) {
        this.languageClient = (JakartaLanguageClientAPI) languageClient;
        this.capabilityManager = new JakartaCapabilityManager(languageClient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getParentProcessId() {
        return parentProcessId != null ? parentProcessId : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ProjectLabelInfoEntry> getJavaProjectLabels(
                                                                         JakartaJavaProjectLabelsParams javaParams) {
        return getLanguageClient().getJavaProjectLabels(javaParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<ProjectLabelInfoEntry>> getAllJavaProjectLabels() {
        return getLanguageClient().getAllJavaProjectLabels();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<JakartaJavaFileInfo> getJavaFileInfo(JakartaJavaFileInfoParams javaParams) {
        return getLanguageClient().getJavaFileInfo(javaParams);
    }

    /**
     * Returns the object that manages dynamic capabilities.
     *
     * @return The object that manages dynamic capabilities.
     */
    public JakartaCapabilityManager getCapabilityManager() {
        return capabilityManager;
    }

    @Override
    public void setTrace(SetTraceParams params) {
        // to avoid having UnsupportedOperationException, the method is implemented
        // FIXME : implement the behavior of this method.
    }
}
