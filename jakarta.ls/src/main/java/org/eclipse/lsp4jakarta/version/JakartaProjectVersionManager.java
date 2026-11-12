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
import java.util.Comparator;
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
 * version), user selection. If the user cancels the first selection of a
 * project, the default version is applied; if the project had a version before
 * (selection after a reset), that version is kept.
 * </p>
 */
public class JakartaProjectVersionManager {

    private static final Logger LOGGER = Logger.getLogger(JakartaProjectVersionManager.class.getName());

    private static final String DEFAULT_VERSION_PROPERTY_KEY = "jakarta.default.version";
    private static final String LOWEST = "lowest";
    private static final String HIGHEST = "highest";
    private static final String DEFAULT_VERSION_STRATEGY = loadDefaultVersionStrategy();

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
                    // If null (cancelled), the request that sent the prompt applies the default and revalidates
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
            LOGGER.info("Client does not support version selection. Using the " + DEFAULT_VERSION_STRATEGY + " detected Jakarta EE version for project: " + projectUri);
            store(projectUri, new VersionData(selectDefaultVersion(detectedVersions, DEFAULT_VERSION_STRATEGY), SelectionMode.DEFAULT, versions));
        } else {
            promptForVersionSelection(projectUri, context, detectedVersions, versions);
            return;
        }
        onVersionResolved.accept(projectUri);
    }

    /**
     * Asks the client to select one of the given versions and, once answered,
     * stores the choice and notifies {@code onVersionResolved}. The pending
     * request is tracked so it can be cancelled by {@link #clear(String)}.
     * A null answer means the user cancelled the selection, in which case the
     * default version is applied.
     *
     * @param projectUri the project URI
     * @param context description of the caller, used in log messages
     * @param detectedVersions the versions detected on the project's classpath
     * @param versions the version labels the user can choose from
     */
    private void promptForVersionSelection(String projectUri, String context, List<JakartaVersion> detectedVersions, List<String> versions) {
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
                    LOGGER.info("Jakarta EE version selection cancelled (" + context + "). Using the " + DEFAULT_VERSION_STRATEGY + " detected Jakarta EE version for project: "
                                + projectUri);
                    store(projectUri, new VersionData(selectDefaultVersion(detectedVersions, DEFAULT_VERSION_STRATEGY), SelectionMode.DEFAULT, versions));
                    onVersionResolved.accept(projectUri);
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
     * Picks the default version among the detected ones.
     *
     * @param detectedVersions the versions detected on the project's classpath
     * @param strategy {@value #LOWEST} to pick the lowest version, anything else picks the highest
     * @return the label of the chosen version, or the label of Jakarta EE 9 if nothing was detected
     */
    static String selectDefaultVersion(List<JakartaVersion> detectedVersions, String strategy) {
        Comparator<JakartaVersion> byLevel = Comparator.comparingInt(JakartaVersion::getLevel);
        return detectedVersions.stream().max(LOWEST.equals(strategy) ? byLevel.reversed() : byLevel).map(JakartaVersion::getLabel).orElse(JakartaVersion.EE_9.getLabel());
    }

    /**
     * Loads the default version strategy ({@value #LOWEST} or {@value #HIGHEST}) from application.properties.
     * Falls back to {@value #HIGHEST} if the file or property is missing or has another value.
     */
    private static String loadDefaultVersionStrategy() {
        try (InputStream input = JakartaProjectVersionManager.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                String value = properties.getProperty(DEFAULT_VERSION_PROPERTY_KEY);
                if (value != null && LOWEST.equalsIgnoreCase(value.trim())) {
                    return LOWEST;
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warning("Could not load application.properties, using the " + HIGHEST + " detected version as default: " + e.getMessage());
        }
        return HIGHEST;
    }
}
