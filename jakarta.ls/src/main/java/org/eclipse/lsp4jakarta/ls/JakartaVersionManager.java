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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Manages Jakarta EE version persistence to project directory.
 * Stores the selected version in a .jakarta-version file in the project root.
 * Uses Java NIO for cross-platform compatibility (Windows, Linux, macOS).
 */
public class JakartaVersionManager {

    private static final Logger LOGGER = Logger.getLogger(JakartaVersionManager.class.getName());
    private static final String VERSION_FILE_NAME = ".jakarta-version";

    // Available Jakarta EE versions
    private static final List<String> JAKARTA_VERSIONS = Arrays.asList(
                                                                       "11.0", "10.0", "9.1", "9.0");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Returns the list of available Jakarta EE versions.
     *
     * @return an unmodifiable list of available Jakarta EE versions
     */
    public static List<String> getAvailableVersions() {
        return JAKARTA_VERSIONS;
    }

    /**
     * Reads the Jakarta EE version data from the project's version file.
     * Uses Java NIO for cross-platform file reading.
     * Parses JSON format with version, selectionMode, and availableVersions.
     *
     * @param projectUri The project URI
     * @return The VersionData object if found, null otherwise
     */
    public static VersionData readVersionData(String projectUri) {
        if (projectUri == null) {
            return null;
        }

        try {
            Path versionFilePath = getVersionFilePath(projectUri);
            if (versionFilePath == null || !Files.exists(versionFilePath)) {
                return null;
            }

            // Read file content
            String content = new String(Files.readAllBytes(versionFilePath), StandardCharsets.UTF_8);

            // Try to parse as JSON first
            try {
                VersionData versionData = GSON.fromJson(content, VersionData.class);
                if (versionData != null && versionData.getVersion() != null && !versionData.getVersion().isEmpty()) {
                    LOGGER.info("Read Jakarta EE version data " + versionData.getVersion() + " from " + versionFilePath);
                    return versionData;
                }
            } catch (JsonSyntaxException e) {
                // Fall back to plain text format for backward compatibility
                String version = content.trim();
                if (!version.isEmpty()) {
                    LOGGER.info("Read Jakarta EE version " + version + " from " + versionFilePath + " (legacy format)");
                    // Create VersionData from legacy format
                    return new VersionData(version, "selected", getAvailableVersions());
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to read Jakarta version file for project " + projectUri + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Reads the Jakarta EE version from the project's version file.
     * Uses Java NIO for cross-platform file reading.
     * Parses JSON format with version, selectionMode, and availableVersions.
     *
     * @param projectUri The project URI
     * @return The version string if found, null otherwise
     */
    public static String readVersion(String projectUri) {
        VersionData versionData = readVersionData(projectUri);
        return versionData != null ? versionData.getVersion() : null;
    }

    /**
     * Writes the Jakarta EE version to the project's version file.
     * Uses Java NIO for cross-platform file writing with proper line endings.
     * Writes JSON format with version, versionType, and availableVersions.
     *
     * @param projectUri The project URI
     * @param version The version to write
     * @return true if successful, false otherwise
     */
    public static boolean writeVersion(String projectUri, VersionData versionData) {
        if (projectUri == null || versionData == null) {
            return false;
        }

        try {
            Path versionFilePath = getVersionFilePath(projectUri);
            if (versionFilePath == null) {
                return false;
            }

            // Ensure parent directory exists
            Path parentDir = versionFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // Convert to JSON
            String jsonContent = GSON.toJson(versionData);

            // Write using NIO
            Files.write(versionFilePath, jsonContent.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to write Jakarta version file for project " + projectUri + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the version file for a project.
     * Uses Java NIO for cross-platform file deletion.
     *
     * @param projectUri The project URI
     * @return true if deleted or didn't exist, false on error
     */
    public static boolean deleteVersion(String projectUri) {
        if (projectUri == null) {
            return true;
        }

        try {
            Path versionFilePath = getVersionFilePath(projectUri);
            if (versionFilePath == null) {
                return true;
            }

            if (Files.exists(versionFilePath)) {
                Files.delete(versionFilePath);
                LOGGER.info("Deleted Jakarta version file: " + versionFilePath);
            }
            return true;
        } catch (IOException e) {
            LOGGER.warning("Failed to delete Jakarta version file for project " + projectUri + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the path to the version file for a project.
     * Handles various URI formats and normalizes paths for cross-platform compatibility.
     *
     * @param projectUri The project URI (can be file:// URI or plain path)
     * @return The path to the version file, or null if invalid URI
     */
    private static Path getVersionFilePath(String projectUri) {
        try {
            Path projectPath = uriToPath(projectUri);
            if (projectPath == null) {
                return null;
            }

            // If it's a file, get its parent directory
            if (Files.isRegularFile(projectPath)) {
                projectPath = projectPath.getParent();
            }

            // Resolve the version file name (handles path separators correctly on all platforms)
            return projectPath.resolve(VERSION_FILE_NAME);
        } catch (Exception e) {
            LOGGER.warning("Failed to resolve version file path for URI " + projectUri + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a URI string to a Path, handling various formats.
     * Supports file:// URIs and plain file paths on Windows, Linux, and macOS.
     *
     * @param uriString The URI string
     * @return The Path object, or null if conversion fails
     */
    private static Path uriToPath(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return null;
        }

        try {
            // Handle file:// URIs
            if (uriString.startsWith("file://") || uriString.startsWith("file:")) {
                URI uri = new URI(uriString);
                return Paths.get(uri);
            }

            // Handle plain paths (works on all platforms)
            return Paths.get(uriString).toAbsolutePath().normalize();
        } catch (URISyntaxException | IllegalArgumentException e) {
            LOGGER.warning("Failed to convert URI to Path: " + uriString + " - " + e.getMessage());
            return null;
        }
    }
}
