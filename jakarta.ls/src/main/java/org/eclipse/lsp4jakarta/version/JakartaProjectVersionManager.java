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
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages the Jakarta EE version of each project.
 * <p>
 * It keeps the selected versions in memory, persists them through
 * {@link JakartaVersionFileStore} and asks the client to choose a version when
 * the project supports several. All operations on a project are serialized with
 * a per-project lock.
 * </p>
 * <p>
 * A project's version is resolved in this order: memory cache, version file,
 * single detected version, default version (when the client cannot select a
 * version), user selection.
 * </p>
 */
public class JakartaProjectVersionManager {

    private static final Logger LOGGER = Logger.getLogger(JakartaProjectVersionManager.class.getName());

    private static final String DEFAULT_VERSION_PROPERTY_KEY = "jakarta.default.version";
    private static final String DEFAULT_VERSION_LABEL = loadDefaultVersionLabel();

    private final Map<String, VersionData> projectVersions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> versionRequestsInFlight = new ConcurrentHashMap<>();
    private final Map<String, Object> projectLocks = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Function<Map<String, Object>, CompletableFuture<String>> versionSelector;
    private final BooleanSupplier versionSelectorSupported;
    private final Consumer<String> onVersionResolved;

    /**
     * Creates a version manager.
     *
     * @param versionSelector sends the version selection request to the client and returns the chosen version
     * @param versionSelectorSupported tells whether the client is able to let the user select a version
     * @param onVersionResolved called with the project URI each time a version is set asynchronously
     */
    public JakartaProjectVersionManager(Function<Map<String, Object>, CompletableFuture<String>> versionSelector,
                                        BooleanSupplier versionSelectorSupported,
                                        Consumer<String> onVersionResolved) {
        this.versionSelector = versionSelector;
        this.versionSelectorSupported = versionSelectorSupported;
        this.onVersionResolved = onVersionResolved;
    }

    /**
     * Returns the executor used for asynchronous version handling.
     *
     * @return the executor, shut down by {@link #shutdown()}
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Returns the cached version data of a project.
     *
     * @param projectUri the project URI, may be null
     * @return the version data, or null if the project has none or the URI is null
     */
    public VersionData getVersionData(String projectUri) {
        return projectUri == null ? null : projectVersions.get(projectUri);
    }

    /**
     * Returns the numeric level of a project's selected version, as used for snippet filtering.
     *
     * @param projectUri the project URI, may be null
     * @return the level as a string (for example "10"), or null if the project has no version
     */
    public String getSelectedVersionLevel(String projectUri) {
        VersionData versionData = getVersionData(projectUri);
        return versionData == null ? null : String.valueOf(JakartaVersion.fromLabel(versionData.getVersion()).getLevel());
    }

    /**
     * Ensures that a version is available for the project.
     * <p>
     * The version is taken from the cache or the version file when possible.
     * Otherwise it is set or requested asynchronously and the
     * {@code onVersionResolved} callback is invoked once it is known. If a
     * selection request is already pending, no new request is sent.
     * </p>
     *
     * @param projectUri the project URI
     * @param detectedVersions the versions detected on the project's classpath
     * @return true if the version is already available, false if it is still being resolved
     */
    public boolean resolve(String projectUri, List<JakartaVersion> detectedVersions) {
        synchronized (lockFor(projectUri)) {
            if (projectVersions.containsKey(projectUri)) {
                return true;
            }

            VersionData versionData = JakartaVersionFileStore.readVersionData(projectUri);
            if (versionData != null) {
                projectVersions.put(projectUri, versionData);
                LOGGER.info("Loaded Jakarta EE version " + versionData.getVersion() + " from file for project: " + projectUri);
                return true;
            }

            CompletableFuture<String> existingRequest = versionRequestsInFlight.get(projectUri);
            if (existingRequest != null) {
                existingRequest.thenAcceptAsync(version -> {
                    if (version != null) {
                        onVersionResolved.accept(projectUri);
                    }
                    // If null (cancelled), do nothing - will retry on next open
                }, executor);
                return false;
            }

            applyDetectedVersions(projectUri, detectedVersions, "initial selection");
            return false;
        }
    }

    /**
     * Forgets the version of a project: removes it from the cache, deletes the
     * version file and cancels any pending selection request.
     *
     * @param projectUri the project URI
     */
    public void clear(String projectUri) {
        synchronized (lockFor(projectUri)) {
            VersionData oldVersionData = projectVersions.remove(projectUri);
            if (oldVersionData != null) {
                LOGGER.info("Cleared Jakarta EE version " + oldVersionData.getVersion() + " from cache for project: " + projectUri);
            }

            if (JakartaVersionFileStore.deleteVersion(projectUri)) {
                LOGGER.info("Deleted version file for project: " + projectUri);
            }

            CompletableFuture<String> existingRequest = versionRequestsInFlight.remove(projectUri);
            if (existingRequest != null && !existingRequest.isDone()) {
                existingRequest.cancel(true);
                LOGGER.info("Cancelled in-flight version request for project: " + projectUri);
            }
        }
    }

    /**
     * Sets or requests the version of a project after {@link #clear(String)},
     * based on the freshly detected versions. Unlike {@link #resolve}, it does
     * not consult the cache or the version file.
     *
     * @param projectUri the project URI
     * @param detectedVersions the versions detected on the project's classpath
     */
    public void resolveAfterReset(String projectUri, List<JakartaVersion> detectedVersions) {
        synchronized (lockFor(projectUri)) {
            applyDetectedVersions(projectUri, detectedVersions, "reset");
        }
    }

    /**
     * Clears the in-memory version cache of all projects. Version files on disk are kept.
     */
    public void clearCache() {
        projectVersions.clear();
    }

    /**
     * Releases all resources: cancels pending selection requests, clears the
     * caches and shuts down the executor, waiting up to 5 seconds for running tasks.
     * Version files on disk are kept so versions survive server restarts.
     */
    public void shutdown() {
        versionRequestsInFlight.values().forEach(future -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
        versionRequestsInFlight.clear();
        projectVersions.clear();
        projectLocks.clear();

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the lock object of a project, creating it on first use.
     *
     * @param projectUri the project URI
     * @return the lock for the project
     */
    private Object lockFor(String projectUri) {
        return projectLocks.computeIfAbsent(projectUri, k -> new Object());
    }

    /**
     * Picks a version from the detected ones: the only one if there is a single
     * version, the default if the client cannot select, otherwise it asks the user.
     * Must be called while holding the project lock.
     *
     * @param projectUri the project URI
     * @param detectedVersions the versions detected on the project's classpath
     * @param context description of the caller, used in log messages
     */
    private void applyDetectedVersions(String projectUri, List<JakartaVersion> detectedVersions, String context) {
        List<String> versions = detectedVersions.stream().map(JakartaVersion::getLabel).collect(Collectors.toList());

        if (versions.size() == 1) {
            store(projectUri, new VersionData(versions.get(0), SelectionMode.SINGLE_VERSION, versions));
        } else if (!versionSelectorSupported.getAsBoolean()) {
            LOGGER.info("Client does not support version selection. Using default Jakarta EE version for project: " + projectUri);
            store(projectUri, new VersionData(DEFAULT_VERSION_LABEL, SelectionMode.DEFAULT, versions));
        } else {
            promptForVersionSelection(projectUri, context, versions);
            return;
        }
        onVersionResolved.accept(projectUri);
    }

    /**
     * Asks the client to select one of the given versions and, once answered,
     * stores the choice and notifies {@code onVersionResolved}. The pending
     * request is tracked so it can be cancelled by {@link #clear(String)}.
     * A null answer means the user cancelled and leaves the project without a version.
     *
     * @param projectUri the project URI
     * @param context description of the caller, used in log messages
     * @param versions the version labels the user can choose from
     */
    private void promptForVersionSelection(String projectUri, String context, List<String> versions) {
        Object projectLock = lockFor(projectUri);

        Map<String, Object> params = new HashMap<>();
        params.put("projectUri", projectUri);
        params.put("versions", versions);

        CompletableFuture<String> versionRequest = versionSelector.apply(params);
        versionRequestsInFlight.put(projectUri, versionRequest);

        versionRequest.thenAcceptAsync(selectedVersion -> {
            synchronized (projectLock) {
                versionRequestsInFlight.remove(projectUri);

                if (selectedVersion != null) {
                    store(projectUri, new VersionData(selectedVersion, SelectionMode.USER_SELECTED, versions));
                    onVersionResolved.accept(projectUri);
                } else {
                    LOGGER.info("Jakarta EE version selection cancelled (" + context + ") for project: " + projectUri);
                }
            }
        }, executor).exceptionally(ex -> {
            synchronized (projectLock) {
                versionRequestsInFlight.remove(projectUri);
                LOGGER.severe("Error during version selection (" + context + ") for project " + projectUri + ": " + ex.getMessage());
            }
            return null;
        });
    }

    /**
     * Caches the version data and persists it to the project's version file.
     * A failed write is logged; the in-memory value is kept.
     *
     * @param projectUri the project URI
     * @param versionData the version data to store
     */
    private void store(String projectUri, VersionData versionData) {
        projectVersions.put(projectUri, versionData);
        if (JakartaVersionFileStore.writeVersion(projectUri, versionData)) {
            LOGGER.info("Jakarta EE version " + versionData.getVersion() + " (" + versionData.getSelectionMode() + ") saved for project: " + projectUri);
        } else {
            LOGGER.warning("Jakarta EE version " + versionData.getVersion() + " set but failed to save to file for project: " + projectUri);
        }
    }

    /**
     * Loads the default Jakarta EE version label from application.properties.
     * Falls back to Jakarta EE 9 if the file or property is not found.
     */
    private static String loadDefaultVersionLabel() {
        try (InputStream input = JakartaProjectVersionManager.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                String value = properties.getProperty(DEFAULT_VERSION_PROPERTY_KEY);
                if (value != null) {
                    // Do not use JakartaVersion.fromString here: at runtime the JakartaVersion class from the
                    // jakarta.jdt.core jar is loaded and it does not have that method (NoSuchMethodError).
                    String trimmed = value.trim();
                    for (JakartaVersion version : JakartaVersion.values()) {
                        if (version != JakartaVersion.UNKNOWN
                            && (String.valueOf(version.getLevel()).equals(trimmed) || version.getLabel().equalsIgnoreCase(trimmed))) {
                            return version.getLabel();
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warning("Could not load application.properties, falling back to Jakarta EE 9: " + e.getMessage());
        }
        return JakartaVersion.EE_9.getLabel();
    }
}
