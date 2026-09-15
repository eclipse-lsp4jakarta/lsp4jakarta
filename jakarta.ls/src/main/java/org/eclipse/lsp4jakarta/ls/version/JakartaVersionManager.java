/*******************************************************************************
* Copyright (c) 2020, 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/

package org.eclipse.lsp4jakarta.ls.version;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.eclipse.lsp4jakarta.ls.VersionData;

/**
 * Manages Jakarta EE version detection, selection, and persistence.
 */
public class JakartaVersionManager {

    private static volatile JakartaVersionManager instance;

    private static final Logger LOGGER = Logger.getLogger(JakartaVersionManager.class.getName());
    private static final String VERSION_FILE_NAME = ".jakarta-version";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, List<JakartaVersion>> projectVersionMap;

    private JakartaVersionManager() {
        projectVersionMap = new HashMap<>();
    }

    public static JakartaVersionManager getInstance() {
        if (instance == null) {
            synchronized (JakartaVersionManager.class) {
                if (instance == null) {
                    instance = new JakartaVersionManager();
                }
            }
        }
        return instance;
    }

    public synchronized void setVersion(String projectName, List<JakartaVersion> versions) {
        projectVersionMap.put(projectName, versions);
    }

    public synchronized List<String> getAvailableVersions(String projectName, List<String> entries) {
        if (!hasVersion(projectName)) {
            setVersion(projectName, JakartaVersionFinder.analyzeClasspathVersions(entries));
        }
        return toVersionStrings(projectVersionMap.get(projectName));
    }

    public synchronized List<String> getAvailableVersions(String projectName) {
        return toVersionStrings(projectVersionMap.get(projectName));
    }

    private List<String> toVersionStrings(List<JakartaVersion> versions) {
        if (versions == null) {
            return List.of();
        }
        return versions.stream().map(version -> version.getLevel() + ".0").toList();
    }

    public synchronized boolean hasVersion(String projectName) {
        return projectVersionMap.containsKey(projectName);
    }

    public synchronized void removeVersion(String projectName) {
        projectVersionMap.remove(projectName);
    }

    public synchronized Map<String, List<JakartaVersion>> getAllVersions() {
        return new HashMap<>(projectVersionMap);
    }

    public synchronized void clearVersions() {
        projectVersionMap.clear();
    }

    public synchronized int getVersionCount() {
        return projectVersionMap.size();
    }

    public static VersionData readVersionData(String projectUri) {
        if (projectUri == null) {
            return null;
        }
        try {
            Path versionFilePath = getVersionFilePath(projectUri);
            if (versionFilePath == null || !Files.exists(versionFilePath)) {
                return null;
            }

            String content = new String(Files.readAllBytes(versionFilePath), StandardCharsets.UTF_8);
            try {
                VersionData versionData = GSON.fromJson(content, VersionData.class);
                if (versionData != null && versionData.getVersion() != null && !versionData.getVersion().isEmpty()) {
                    LOGGER.info("Read Jakarta EE version data " + versionData.getVersion() + " from " + versionFilePath);
                    return versionData;
                }
            } catch (JsonSyntaxException e) {
                String version = content.trim();
                if (!version.isEmpty()) {
                    LOGGER.info("Read Jakarta EE version " + version + " from " + versionFilePath + " (legacy format)");
                    return new VersionData(version, "selected", List.of(version));
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to read Jakarta version file for project " + projectUri + ": " + e.getMessage());
        }
        return null;
    }

    public static String readVersion(String projectUri) {
        VersionData versionData = readVersionData(projectUri);
        return versionData != null ? versionData.getVersion() : null;
    }

    public static boolean writeVersion(String projectUri, VersionData versionData) {
        if (projectUri == null || versionData == null) {
            return false;
        }
        try {
            Path versionFilePath = getVersionFilePath(projectUri);
            if (versionFilePath == null) {
                return false;
            }
            Path parentDir = versionFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            Files.write(versionFilePath, GSON.toJson(versionData).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to write Jakarta version file for project " + projectUri + ": " + e.getMessage());
            return false;
        }
    }

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

    private static Path getVersionFilePath(String projectUri) {
        try {
            Path projectPath = uriToPath(projectUri);
            if (projectPath == null) {
                return null;
            }
            if (Files.isRegularFile(projectPath)) {
                projectPath = projectPath.getParent();
            }
            return projectPath.resolve(VERSION_FILE_NAME);
        } catch (Exception e) {
            LOGGER.warning("Failed to resolve version file path for URI " + projectUri + ": " + e.getMessage());
            return null;
        }
    }

    private static Path uriToPath(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return null;
        }
        try {
            if (uriString.startsWith("file://") || uriString.startsWith("file:")) {
                return Paths.get(new URI(uriString));
            }
            return Paths.get(uriString).toAbsolutePath().normalize();
        } catch (URISyntaxException | IllegalArgumentException e) {
            LOGGER.warning("Failed to convert URI to Path: " + uriString + " - " + e.getMessage());
            return null;
        }
    }
}
