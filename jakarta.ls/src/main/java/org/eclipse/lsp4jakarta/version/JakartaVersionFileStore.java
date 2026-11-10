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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Persists the selected Jakarta EE version of a project as JSON in the
 * {@value #VERSION_DIR_NAME}/{@value #VERSION_FILE_NAME} file under the project root.
 */
public class JakartaVersionFileStore {

    private static final Logger LOGGER = Logger.getLogger(JakartaVersionFileStore.class.getName());

    private static final String VERSION_DIR_NAME = ".jakartals";
    private static final String VERSION_FILE_NAME = "jakarta-version";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JakartaVersionFileStore() {}

    /**
     * Reads the version data (version, selectionMode, availableVersions) from the
     * project's version file.
     *
     * @param projectUri the project URI (file URI or plain path)
     * @return the version data, or null if the file is missing, unreadable or has
     *         no version
     */
    public static VersionData readVersionData(String projectUri) {
        Path versionFile = getVersionFilePath(projectUri);
        if (versionFile == null || !Files.exists(versionFile)) {
            return null;
        }

        try {
            // Strip UTF-8 BOM if present
            String content = Files.readString(versionFile).replaceFirst("^\uFEFF", "");

            VersionData versionData = GSON.fromJson(content, VersionData.class);
            if (versionData != null
                && versionData.getVersion() != null
                && !versionData.getVersion().isEmpty()) {

                LOGGER.info("Read Jakarta EE version data "
                            + versionData.getVersion()
                            + " from "
                            + versionFile);

                return versionData;
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.warning("Failed to read Jakarta version file for project "
                           + projectUri + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Writes the version data as JSON to the project's version file.
     *
     * @param projectUri the project URI (file URI or plain path)
     * @param versionData the version data to write
     * @return true if successful, false otherwise
     */
    public static boolean writeVersion(String projectUri, VersionData versionData) {
        Path versionFile = versionData == null ? null : getVersionFilePath(projectUri);
        if (versionFile == null) {
            return false;
        }

        try {
            Files.createDirectories(versionFile.getParent());
            Files.writeString(versionFile, GSON.toJson(versionData));
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to write Jakarta version file for project "
                          + projectUri + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the version file of a project.
     *
     * @param projectUri the project URI (file URI or plain path)
     * @return true if deleted or nothing to delete, false on error
     */
    public static boolean deleteVersion(String projectUri) {
        Path versionFile = getVersionFilePath(projectUri);
        if (versionFile == null) {
            return true;
        }

        try {
            if (Files.deleteIfExists(versionFile)) {
                LOGGER.info("Deleted Jakarta version file: " + versionFile);
            }
            return true;
        } catch (IOException e) {
            LOGGER.warning("Failed to delete Jakarta version file for project "
                           + projectUri + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Converts a file URI or a plain OS path to a normalized absolute path.
     * <p>
     * Works the same on Windows, Linux and macOS: it accepts {@code file:///C:/dir},
     * {@code file:///c%3A/dir} and {@code file:///home/dir} URIs as well as plain
     * paths such as {@code C:\dir} or {@code /home/dir}. Other URI schemes
     * (for example {@code jdt://}) are rejected.
     * </p>
     *
     * @param uriOrPath the file URI or plain path
     * @return the path, or null if the value is null, blank, not a file location or invalid
     */
    public static Path toPath(String uriOrPath) {
        if (uriOrPath == null || uriOrPath.isBlank()) {
            return null;
        }

        try {
            if (uriOrPath.regionMatches(true, 0, "file:", 0, 5)) {
                return Path.of(URI.create(uriOrPath)).toAbsolutePath().normalize();
            }
            if (uriOrPath.contains("://")) {
                return null;
            }
            return Path.of(uriOrPath).toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            LOGGER.warning("Failed to convert to path: " + uriOrPath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Resolves the version file path for a project. If the project location is
     * an existing file, the version file is resolved against its parent directory.
     *
     * @param projectUri the project URI (file URI or plain path)
     * @return the version file path, or null if the URI is null, blank, or invalid
     */
    private static Path getVersionFilePath(String projectUri) {
        Path projectPath = toPath(projectUri);
        if (projectPath == null) {
            return null;
        }

        if (Files.isRegularFile(projectPath)) {
            projectPath = projectPath.getParent();
        }
        return projectPath.resolve(VERSION_DIR_NAME).resolve(VERSION_FILE_NAME);
    }
}