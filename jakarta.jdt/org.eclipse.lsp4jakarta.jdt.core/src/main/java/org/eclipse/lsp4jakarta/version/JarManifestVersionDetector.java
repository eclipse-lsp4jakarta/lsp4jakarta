package org.eclipse.lsp4jakarta.version;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Detects Jakarta EE version by inspecting JAR manifest files.
 * This strategy reads META-INF/MANIFEST.MF from Jakarta JARs to extract
 * version information from manifest attributes like Specification-Version,
 * Implementation-Version, and Bundle-Version.
 */
public class JarManifestVersionDetector {

    /**
     * Analyzes classpath entries to detect Jakarta version from JAR manifests.
     *
     * @param entries The classpath entries to analyze
     * @return The detected Jakarta version based on manifest inspection
     */
    public JakartaVersion detectVersion(IClasspathEntry[] entries) {
        JakartaVersion detectedVersion = JakartaVersion.UNKNOWN;
        Set<String> manifestVersions = new HashSet<>();

        for (IClasspathEntry entry : entries) {
            IPath path = entry.getPath();
            String jarPath = path.toOSString();

            // Only process JAR files
            if (jarPath.isEmpty() || !jarPath.endsWith(".jar")) {
                continue;
            }

            try {
                JakartaVersion manifestVersion = extractVersionFromManifest(jarPath);
                if (manifestVersion != JakartaVersion.UNKNOWN) {
                    manifestVersions.add(jarPath + " -> " + manifestVersion.getLabel());
                    if (manifestVersion.getLevel() > detectedVersion.getLevel()) {
                        detectedVersion = manifestVersion;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading manifest from: " + jarPath + " - " + e.getMessage());
            }
        }

        System.out.println("JAR Manifest Analysis Results-----------");
        manifestVersions.forEach(System.out::println);
        System.out.println("------------------------------------------------------------------");
        System.out.println("Manifest Detected Version: " + detectedVersion.getLabel() + "-" + detectedVersion.getLevel());
        System.out.println("------------------------------------------------------------------");

        return detectedVersion;
    }

    /**
     * Extracts Jakarta version from a JAR file's manifest.
     * Reads META-INF/MANIFEST.MF and checks various version attributes.
     *
     * @param jarPath The path to the JAR file
     * @return The detected Jakarta version from manifest attributes
     * @throws IOException if the JAR cannot be read
     */
    private JakartaVersion extractVersionFromManifest(String jarPath) throws IOException {
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            return JakartaVersion.UNKNOWN;
        }

        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return JakartaVersion.UNKNOWN;
            }

            Attributes mainAttributes = manifest.getMainAttributes();

            // Get Bundle-SymbolicName - this is REQUIRED for detection
            String bundleSymbolicName = mainAttributes.getValue("Bundle-SymbolicName");

            // Skip if Bundle-SymbolicName is not present or not Jakarta-related
            if (bundleSymbolicName == null || bundleSymbolicName.isEmpty()) {
                return JakartaVersion.UNKNOWN;
            }

            if (!isJakartaBundle(bundleSymbolicName)) {
                return JakartaVersion.UNKNOWN;
            }

            // Try different manifest attributes in order of preference
            String version = getVersionFromAttributes(mainAttributes,
                                                      "Specification-Version",
                                                      "Implementation-Version",
                                                      "Bundle-Version",
                                                      "Jakarta-Version");

            if (version != null && !version.isEmpty()) {
                return mapManifestVersionToJakarta(version, bundleSymbolicName);
            }
        }

        return JakartaVersion.UNKNOWN;
    }

    /**
     * Retrieves version string from manifest attributes, trying multiple attribute names.
     *
     * @param attributes The manifest attributes
     * @param attributeNames The attribute names to check in order
     * @return The first non-null version string found, or null if none found
     */
    private String getVersionFromAttributes(Attributes attributes, String... attributeNames) {
        for (String attrName : attributeNames) {
            String value = attributes.getValue(attrName);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Checks if a bundle is Jakarta-related based ONLY on Bundle-SymbolicName.
     *
     * @param bundleSymbolicName The Bundle-SymbolicName from manifest
     * @return true if the bundle is Jakarta-related, false otherwise
     */
    private boolean isJakartaBundle(String bundleSymbolicName) {
        String symbolicName = bundleSymbolicName.toLowerCase();
        return symbolicName.startsWith("jakarta.") ||
               symbolicName.contains("jakartaee");
    }

    /**
     * Maps a manifest version string to a Jakarta EE version.
     * Uses ONLY Bundle-SymbolicName with -api suffix pattern for exact matching.
     *
     * @param version The version string from manifest
     * @param bundleSymbolicName The Bundle-SymbolicName from manifest
     * @return The corresponding Jakarta version
     */
    private JakartaVersion mapManifestVersionToJakarta(String version, String bundleSymbolicName) {
        // Remove any parameters after semicolon (e.g., "jakarta.servlet-api;singleton:=true")
        String artifactIdentifier = bundleSymbolicName.split(";")[0].toLowerCase();
        System.out.println("Using Bundle-SymbolicName: " + artifactIdentifier);

        // Check for Jakarta EE Platform JARs first
        if (artifactIdentifier.equals("jakarta.jakartaee-api") ||
            artifactIdentifier.equals("jakarta.jakartaee-web-api") ||
            artifactIdentifier.equals("jakarta.jakartaee-core-api")) {
            return getBaseVersion(version);
        }

        // Parse version to double for module-specific mapping
        double ver = parseVersionDouble(version);

        // Map module-specific versions using ONLY -api suffix pattern
        if (artifactIdentifier.equals("jakarta.servlet-api")) {
            return mapToJakartaVersion(ver, 6.1, 6.0, 5.0);
        } else if (artifactIdentifier.equals("jakarta.faces-api")) {
            return mapToJakartaVersion(ver, 4.1, 4.0, 3.0);
        } else if (artifactIdentifier.equals("jakarta.ws.rs-api")) {
            return mapToJakartaVersion(ver, 4.0, 3.1, 3.0);
        } else if (artifactIdentifier.equals("jakarta.websocket-api") ||
                   artifactIdentifier.equals("jakarta.websocket-client-api")) {
            return mapToJakartaVersion(ver, 2.2, 2.1, 2.0);
        } else if (artifactIdentifier.equals("jakarta.json-api")) {
            return mapToJakartaVersion(ver, 2.1, 2.1, 2.0);
        } else if (artifactIdentifier.equals("jakarta.json.bind-api")) {
            return mapToJakartaVersion(ver, 3.0, 3.0, 2.0);
        } else if (artifactIdentifier.equals("jakarta.annotation-api")) {
            return mapToJakartaVersion(ver, 3.0, 2.1, 2.0);
        } else if (artifactIdentifier.equals("jakarta.ejb-api")) {
            return mapToJakartaVersion(ver, 4.0, 4.0, 4.0);
        } else if (artifactIdentifier.equals("jakarta.transaction-api")) {
            return mapToJakartaVersion(ver, 2.0, 2.0, 2.0);
        } else if (artifactIdentifier.equals("jakarta.validation.jakarta.validation-api")) {
            return mapToJakartaVersion(ver, 3.2, 3.1, 3.0);
        } else if (artifactIdentifier.equals("jakarta.validation-api")) {
            return mapToJakartaVersion(ver, 3.1, 3.0, 3.0);
        } else if (artifactIdentifier.equals("jakarta.interceptor-api")) {
            return mapToJakartaVersion(ver, 2.2, 2.1, 2.0);
        } else if (artifactIdentifier.equals("jakarta.enterprise.cdi-api")) {
            return mapToJakartaVersion(ver, 4.1, 4.0, 3.0);
        } else if (artifactIdentifier.equals("jakarta.inject.jakarta.inject-api")) {
            return mapToJakartaVersion(ver, 2.0, 2.0, 2.0);
        } else if (artifactIdentifier.equals("jakarta.security.enterprise-api")) {
            return mapToJakartaVersion(ver, 4.0, 3.0, 2.0);
        } else if (artifactIdentifier.equals("jakarta.data-api")) {
            return mapToJakartaVersion(ver, 1.0, 0.0, 0.0);
        }

        return JakartaVersion.UNKNOWN;
    }

    /**
     * Maps base Jakarta EE platform version to JakartaVersion enum.
     *
     * @param version The version string from manifest
     * @return The corresponding Jakarta version
     */
    private JakartaVersion getBaseVersion(String version) {
        if (version.startsWith("11."))
            return JakartaVersion.EE_11;
        if (version.startsWith("10."))
            return JakartaVersion.EE_10;
        if (version.startsWith("9."))
            return JakartaVersion.EE_9;

        return JakartaVersion.UNKNOWN;
    }

    /**
     * Parses version string to double for comparison.
     *
     * @param version The version string
     * @return The version as a double (major.minor)
     */
    private double parseVersionDouble(String version) {
        try {
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                return Double.parseDouble(parts[0] + "." + parts[1]);
            }
            return Double.parseDouble(parts[0]);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Maps a version number to Jakarta EE version based on thresholds.
     *
     * @param ver The version number
     * @param v11 The minimum version for EE 11
     * @param v10 The minimum version for EE 10
     * @param v9 The minimum version for EE 9
     * @return The corresponding Jakarta version
     */
    private JakartaVersion mapToJakartaVersion(double ver, double v11, double v10, double v9) {
        if (ver >= v11)
            return JakartaVersion.EE_11;
        if (ver >= v10)
            return JakartaVersion.EE_10;
        if (ver >= v9)
            return JakartaVersion.EE_9;

        return JakartaVersion.UNKNOWN;
    }
}

// Made with Bob
