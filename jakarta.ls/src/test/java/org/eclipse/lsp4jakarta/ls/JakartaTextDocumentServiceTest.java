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

package org.eclipse.lsp4jakarta.ls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCompletionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCompletionResult;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfo;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfoParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaProjectLabelsParams;
import org.eclipse.lsp4jakarta.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4jakarta.ls.api.JakartaLanguageClientAPI;
import org.eclipse.lsp4jakarta.ls.commons.client.ExtendedClientCapabilities;
import org.eclipse.lsp4jakarta.ls.java.JakartaTextDocuments;
import org.eclipse.lsp4jakarta.settings.SharedSettings;
import org.eclipse.lsp4jakarta.version.JakartaVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JakartaTextDocumentServiceTest {

    private File tempProjectDir;
    private String projectUri;
    private String fileUri;
    private TestLanguageClient languageClient;
    private JakartaLanguageServer languageServer;
    private JakartaTextDocumentService service;

    public static class TestLanguageClient implements JakartaLanguageClientAPI {
        public volatile ProjectLabelInfoEntry projectLabelInfoEntry;
        public volatile CompletableFuture<String> versionSelectionFuture = new CompletableFuture<>();
        public volatile Map<String, Object> lastVersionSelectionParams;
        public final List<PublishDiagnosticsParams> publishedDiagnostics = Collections.synchronizedList(new ArrayList<>());
        public final List<JakartaJavaDiagnosticsParams> requestedDiagnostics = Collections.synchronizedList(new ArrayList<>());

        @Override
        public CompletableFuture<JakartaJavaCompletionResult> getJavaCompletion(JakartaJavaCompletionParams params) {
            return CompletableFuture.completedFuture(new JakartaJavaCompletionResult(new CompletionList(), null));
        }

        @Override
        public CompletableFuture<ProjectLabelInfoEntry> getJavaProjectLabels(JakartaJavaProjectLabelsParams params) {
            return CompletableFuture.completedFuture(projectLabelInfoEntry);
        }

        @Override
        public CompletableFuture<List<ProjectLabelInfoEntry>> getAllJavaProjectLabels() {
            return CompletableFuture.completedFuture(projectLabelInfoEntry != null ? List.of(projectLabelInfoEntry) : Collections.emptyList());
        }

        @Override
        public CompletableFuture<JakartaJavaFileInfo> getJavaFileInfo(JakartaJavaFileInfoParams params) {
            JakartaJavaFileInfo info = new JakartaJavaFileInfo();
            info.setPackageName("com.example");
            return CompletableFuture.completedFuture(info);
        }

        @Override
        public CompletableFuture<List<org.eclipse.lsp4j.CodeAction>> getJavaCodeAction(JakartaJavaCodeActionParams params) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        @Override
        public CompletableFuture<org.eclipse.lsp4j.CodeAction> resolveCodeAction(org.eclipse.lsp4j.CodeAction unresolved) {
            return CompletableFuture.completedFuture(unresolved);
        }

        @Override
        public CompletableFuture<List<PublishDiagnosticsParams>> getJavaDiagnostics(JakartaJavaDiagnosticsParams params) {
            requestedDiagnostics.add(params);
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            publishedDiagnostics.add(diagnostics);
        }

        @Override
        public CompletableFuture<String> selectJakartaVersion(Map<String, Object> params) {
            this.lastVersionSelectionParams = params;
            return versionSelectionFuture;
        }

        @Override
        public void telemetryEvent(Object object) {}

        @Override
        public void logMessage(org.eclipse.lsp4j.MessageParams message) {}

        @Override
        public void showMessage(org.eclipse.lsp4j.MessageParams messageParams) {}

        @Override
        public CompletableFuture<org.eclipse.lsp4j.MessageActionItem> showMessageRequest(org.eclipse.lsp4j.ShowMessageRequestParams requestParams) {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Before
    public void setUp() throws IOException {
        tempProjectDir = Files.createTempDirectory("lsp4jakarta-test-project-").toFile();
        projectUri = tempProjectDir.toURI().toString();
        // Ensure standard file:/// URI format
        if (!projectUri.startsWith("file:///")) {
            projectUri = projectUri.replaceFirst("file:/+", "file:///");
        }
        fileUri = new File(tempProjectDir, "TestServlet.java").toURI().toString();
        if (!fileUri.startsWith("file:///")) {
            fileUri = fileUri.replaceFirst("file:/+", "file:///");
        }

        languageClient = new TestLanguageClient();
        languageServer = new JakartaLanguageServer();
        languageServer.setLanguageClient(languageClient);

        SharedSettings settings = new SharedSettings();
        org.eclipse.lsp4j.HoverCapabilities hoverCaps = new org.eclipse.lsp4j.HoverCapabilities();
        hoverCaps.setContentFormat(List.of("markdown", "plaintext"));
        settings.getHoverSettings().setCapabilities(hoverCaps);

        JakartaTextDocuments documents = new JakartaTextDocuments(languageServer, languageServer);
        service = new JakartaTextDocumentService(languageServer, settings, documents);
    }

    @After
    public void tearDown() {
        if (service != null) {
            service.shutdown();
        }
        if (tempProjectDir != null && tempProjectDir.exists()) {
            JakartaVersionManager.deleteVersion(projectUri);
            deleteRecursively(tempProjectDir);
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    @Test
    public void testDidOpenWithSingleVersion() throws Exception {
        languageClient.projectLabelInfoEntry = new ProjectLabelInfoEntry(projectUri, "test-project", List.of("jakarta"), List.of(JakartaVersion.EE_10));

        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(new TextDocumentItem(fileUri, "java", 1, "package com.example;\npublic class TestServlet {}"));

        service.didOpen(openParams);

        Thread.sleep(300);

        VersionData versionData = JakartaVersionManager.readVersionData(projectUri);
        assertNotNull("Version file should be written for single version", versionData);
        assertEquals("Jakarta EE 10", versionData.getVersion());
        assertEquals(SelectionMode.SINGLE_VERSION, versionData.getSelectionMode());
    }

    @Test
    public void testDidOpenWithMultipleVersionsClientWithoutSelectorCapability() throws Exception {
        ExtendedClientCapabilities extended = new ExtendedClientCapabilities();
        extended.setJakartaVersionSelector(false);
        service.updateClientCapabilities(new ClientCapabilities(), extended);
        languageServer.getCapabilityManager().setClientCapabilities(new ClientCapabilities(), extended);

        languageClient.projectLabelInfoEntry = new ProjectLabelInfoEntry(projectUri, "test-project", List.of("jakarta"), List.of(JakartaVersion.EE_10, JakartaVersion.EE_11));

        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(new TextDocumentItem(fileUri, "java", 1, "package com.example;\npublic class TestServlet {}"));

        service.didOpen(openParams);

        Thread.sleep(300);

        VersionData versionData = JakartaVersionManager.readVersionData(projectUri);
        assertNotNull("Version file should be written with default version", versionData);
        assertEquals("Jakarta EE 9 / 9.1", versionData.getVersion());
        assertEquals(SelectionMode.DEFAULT, versionData.getSelectionMode());
    }

    @Test
    public void testDidOpenWithMultipleVersionsClientWithSelectorCapability() throws Exception {
        ExtendedClientCapabilities extended = new ExtendedClientCapabilities();
        extended.setJakartaVersionSelector(true);
        service.updateClientCapabilities(new ClientCapabilities(), extended);
        languageServer.getCapabilityManager().setClientCapabilities(new ClientCapabilities(), extended);

        languageClient.projectLabelInfoEntry = new ProjectLabelInfoEntry(projectUri, "test-project", List.of("jakarta"), List.of(JakartaVersion.EE_10, JakartaVersion.EE_11));

        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(new TextDocumentItem(fileUri, "java", 1, "package com.example;\npublic class TestServlet {}"));

        service.didOpen(openParams);

        Thread.sleep(200);

        assertNotNull("Should prompt client for version selection", languageClient.lastVersionSelectionParams);

        languageClient.versionSelectionFuture.complete("Jakarta EE 11");

        Thread.sleep(300);

        VersionData versionData = JakartaVersionManager.readVersionData(projectUri);
        assertNotNull("Version file should be written after client selection", versionData);
        assertEquals("Jakarta EE 11", versionData.getVersion());
        assertEquals(SelectionMode.USER_SELECTED, versionData.getSelectionMode());
    }

    @Test
    public void testResetVersionAndRevalidateWithSingleVersion() throws Exception {
        VersionData initialData = new VersionData("Jakarta EE 11", SelectionMode.USER_SELECTED, List.of("Jakarta EE 10", "Jakarta EE 11"));
        JakartaVersionManager.writeVersion(projectUri, initialData);

        languageClient.projectLabelInfoEntry = new ProjectLabelInfoEntry(projectUri, "test-project", List.of("jakarta"), List.of(JakartaVersion.EE_10));

        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(new TextDocumentItem(fileUri, "java", 1, "package com.example;\npublic class TestServlet {}"));
        service.didOpen(openParams);

        Thread.sleep(200);

        service.resetVersionAndRevalidate(projectUri);

        Thread.sleep(300);

        VersionData resetData = JakartaVersionManager.readVersionData(projectUri);
        assertNotNull(resetData);
        assertEquals("Jakarta EE 10", resetData.getVersion());
        assertEquals(SelectionMode.SINGLE_VERSION, resetData.getSelectionMode());
    }

    @Test
    public void testResetVersionAndRevalidateWithoutSelectorCapability() throws Exception {
        ExtendedClientCapabilities extended = new ExtendedClientCapabilities();
        extended.setJakartaVersionSelector(false);
        service.updateClientCapabilities(new ClientCapabilities(), extended);
        languageServer.getCapabilityManager().setClientCapabilities(new ClientCapabilities(), extended);

        languageClient.projectLabelInfoEntry = new ProjectLabelInfoEntry(projectUri, "test-project", List.of("jakarta"), List.of(JakartaVersion.EE_10, JakartaVersion.EE_11));

        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(new TextDocumentItem(fileUri, "java", 1, "package com.example;\npublic class TestServlet {}"));
        service.didOpen(openParams);

        Thread.sleep(200);

        service.resetVersionAndRevalidate(projectUri);

        Thread.sleep(300);

        VersionData resetData = JakartaVersionManager.readVersionData(projectUri);
        assertNotNull(resetData);
        assertEquals("Jakarta EE 9 / 9.1", resetData.getVersion());
        assertEquals(SelectionMode.DEFAULT, resetData.getSelectionMode());
    }

    @Test
    public void testCompletionUsesSelectedVersion() throws Exception {
        VersionData versionData = new VersionData("Jakarta EE 10", SelectionMode.SINGLE_VERSION, List.of("Jakarta EE 10"));
        JakartaVersionManager.writeVersion(projectUri, versionData);

        languageClient.projectLabelInfoEntry = new ProjectLabelInfoEntry(projectUri, "test-project", List.of("jakarta"), List.of(JakartaVersion.EE_10));

        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(new TextDocumentItem(fileUri, "java", 1, "package com.example;\npublic class TestServlet {\n\n}"));
        service.didOpen(openParams);

        Thread.sleep(200);

        CompletionParams completionParams = new CompletionParams(new TextDocumentIdentifier(fileUri), new Position(1, 0));

        CompletableFuture<Either<List<CompletionItem>, CompletionList>> future = service.completion(completionParams);
        Either<List<CompletionItem>, CompletionList> result = future.get(5, TimeUnit.SECONDS);

        assertNotNull(result);
        assertTrue(result.isRight());
        CompletionList completionList = result.getRight();
        assertNotNull(completionList);
    }
}
