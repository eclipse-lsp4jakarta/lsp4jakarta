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

package org.eclipse.lsp4jakarta.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests {@link JakartaVersionFileStore} with plain OS paths and file URIs.
 * Uses only java.nio so it behaves the same on Windows, Linux and macOS.
 */
public class JakartaVersionFileStoreTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private static VersionData sample() {
        return new VersionData("Jakarta EE 10", SelectionMode.USER_SELECTED, Arrays.asList("Jakarta EE 10", "Jakarta EE 9 / 9.1"));
    }

    private Path projectWithSpace() throws IOException {
        return tmp.newFolder("my project").toPath();
    }

    @Test
    public void writeReadDeleteWithPlainPath() throws IOException {
        Path project = projectWithSpace();
        String projectUri = project.toString();

        assertTrue(JakartaVersionFileStore.writeVersion(projectUri, sample()));
        assertTrue(Files.exists(project.resolve(".jakarta-version")));

        VersionData read = JakartaVersionFileStore.readVersionData(projectUri);
        assertNotNull(read);
        assertEquals("Jakarta EE 10", read.getVersion());
        assertEquals(SelectionMode.USER_SELECTED, read.getSelectionMode());
        assertEquals(sample().getAvailableVersions(), read.getAvailableVersions());

        assertTrue(JakartaVersionFileStore.deleteVersion(projectUri));
        assertFalse(Files.exists(project.resolve(".jakarta-version")));
        assertNull(JakartaVersionFileStore.readVersionData(projectUri));
    }

    @Test
    public void writeReadWithFileUriContainingSpace() throws IOException {
        Path project = projectWithSpace();
        String fileUri = project.toUri().toString(); // file:///.../my%20project/

        assertTrue(JakartaVersionFileStore.writeVersion(fileUri, sample()));
        assertTrue(Files.exists(project.resolve(".jakarta-version")));
        assertNotNull(JakartaVersionFileStore.readVersionData(project.toString()));
    }

    @Test
    public void readIgnoresUtf8Bom() throws IOException {
        Path project = projectWithSpace();
        String json = "\uFEFF{\"version\":\"Jakarta EE 11\",\"selectionMode\":\"default\",\"availableVersions\":[]}";
        Files.write(project.resolve(".jakarta-version"), json.getBytes(StandardCharsets.UTF_8));

        VersionData read = JakartaVersionFileStore.readVersionData(project.toString());
        assertNotNull(read);
        assertEquals("Jakarta EE 11", read.getVersion());
        assertEquals(SelectionMode.DEFAULT, read.getSelectionMode());
    }

    @Test
    public void readInvalidOrMissingReturnsNull() throws IOException {
        Path project = projectWithSpace();
        assertNull(JakartaVersionFileStore.readVersionData(project.toString()));

        Files.write(project.resolve(".jakarta-version"), "not json".getBytes(StandardCharsets.UTF_8));
        assertNull(JakartaVersionFileStore.readVersionData(project.toString()));

        assertNull(JakartaVersionFileStore.readVersionData(null));
        assertNull(JakartaVersionFileStore.readVersionData(" "));
        assertFalse(JakartaVersionFileStore.writeVersion(null, sample()));
        assertFalse(JakartaVersionFileStore.writeVersion(project.toString(), null));
        assertTrue(JakartaVersionFileStore.deleteVersion(null));
    }

    @Test
    public void filePathResolvesToParentDirectory() throws IOException {
        Path project = projectWithSpace();
        Path javaFile = Files.createFile(project.resolve("A.java"));

        assertTrue(JakartaVersionFileStore.writeVersion(javaFile.toString(), sample()));
        assertTrue(Files.exists(project.resolve(".jakarta-version")));
    }

    @Test
    public void toPathMatchesFileUriAndPlainPath() throws IOException {
        Path project = projectWithSpace();
        Path fromUri = JakartaVersionFileStore.toPath(project.resolve("src/A.java").toUri().toString());
        Path fromPlain = JakartaVersionFileStore.toPath(project.toString());

        assertNotNull(fromUri);
        assertNotNull(fromPlain);
        // This is how the service finds an open document that belongs to a project
        assertTrue(fromUri.startsWith(fromPlain));
    }

    @Test
    public void toPathDoesNotMatchSiblingProjectWithSamePrefix() throws IOException {
        Path root = tmp.newFolder("ws").toPath();
        Path project = Files.createDirectory(root.resolve("proj"));
        Path sibling = Files.createDirectory(root.resolve("proj2"));

        Path projectPath = JakartaVersionFileStore.toPath(project.toString());
        Path siblingFile = JakartaVersionFileStore.toPath(sibling.resolve("A.java").toUri().toString());

        assertFalse(siblingFile.startsWith(projectPath));
    }

    @Test
    public void toPathRejectsNonFileSchemesAndBlank() {
        assertNull(JakartaVersionFileStore.toPath(null));
        assertNull(JakartaVersionFileStore.toPath(""));
        assertNull(JakartaVersionFileStore.toPath("jdt://jarentry/x.jar"));
    }
}
