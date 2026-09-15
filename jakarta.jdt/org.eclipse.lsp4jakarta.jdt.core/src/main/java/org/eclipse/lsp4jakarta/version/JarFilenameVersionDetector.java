package org.eclipse.lsp4jakarta.version;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Detects Jakarta EE version by parsing JAR filenames from the classpath.
 * This strategy extracts version information from JAR file naming conventions.
 */
public class JarFilenameVersionDetector {

    // Regex to match JAR names and versions: matches "name-version.jar"
    private static final Pattern JAR_PATTERN = Pattern.compile("([^/\\\\]+)-([0-9\\.]+[^/\\\\]*)\\.jar$");

    /**
     * Analyzes classpath entries to detect Jakarta version from JAR filenames.
     *
     * @param entries The classpath entries to analyze
     * @return The detected Jakarta version
     */
    public JakartaVersion detectVersion(IClasspathEntry[] entries) {
        JakartaVersion detectedVersion = JakartaVersion.UNKNOWN;
        Set<String> availableVersions = new HashSet<>();

        for (IClasspathEntry entry : entries) {
            IPath path = entry.getPath();
            String line = path.toOSString();
            System.out.println("Classpath entry: " + line);

            if (line.isEmpty() || !line.endsWith(".jar")) {
                continue;
            }

            Matcher matcher = JAR_PATTERN.matcher(line);
            if (matcher.find()) {
                String artifactName = matcher.group(1);
                String version = matcher.group(2);

                // Priority 1: Check for the Jakarta EE Platform base Jar
                if (artifactName.contains("jakartaee-api") || artifactName.contains("jakartaee-web-api")
                    || artifactName.contains("jakartaee-core-api")) {
                    JakartaVersion v = getBaseVersion(version);
                    availableVersions.add(artifactName + ":" + version + " -> " + v.getLabel());
                    if (v.getLevel() > detectedVersion.getLevel()) {
                        detectedVersion = v;
                    }
                }

                // Priority 2: check module version
                JakartaVersion moduleVersion = getModuleVersion(artifactName, version);
                availableVersions.add(artifactName + ":" + version + " -> " + moduleVersion.getLabel());
                if (moduleVersion.getLevel() > detectedVersion.getLevel()) {
                    detectedVersion = moduleVersion;
                }
            }
        }

        System.out.println("JAR Filename Analysis Results-----------");
        availableVersions.forEach(System.out::println);
        System.out.println("------------------------------------------------------------------");
        System.out.println("Filename Detected Version: " + detectedVersion.getLabel() + "-" + detectedVersion.getLevel());
        System.out.println("------------------------------------------------------------------");

        return detectedVersion;
    }

    /**
     * Maps base Jakarta EE platform version to JakartaVersion enum.
     *
     * @param version The version string from JAR filename
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
     * Maps module-specific version to Jakarta EE version.
     *
     * @param name The artifact name
     * @param version The version string
     * @return The corresponding Jakarta version
     */
    private JakartaVersion getModuleVersion(String name, String version) {
        double ver = parseVersionDouble(version);

        // --- SERVLET ---
        if (name.contains("servlet-api") || name.equals("jakarta.servlet")) {
            return mapToJakartaVersion(ver, 6.1, 6.0, 5.0);
        }

        // jakarta.faces-api || jakarta.faces
        if (name.contains("faces-api") || name.equals("jakarta.faces")) {
            return mapToJakartaVersion(ver, 4.1, 4.0, 3.0);
        }

        // jakarta.ws.rs-api || jakarta.ws.rs
        if (name.contains("ws.rs-api") || name.equals("jakarta.ws.rs")) {
            return mapToJakartaVersion(ver, 4.0, 3.1, 3.0);
        }

        // websocket-api || jakarta.websocket || websocket-client-api || websocket-all - 2.0,2.1,2.2
        if (name.contains("websocket-api") || name.equals("websocket-client-api") || name.equals("websocket-all") || name.equals("jakarta.websocket")) {
            return mapToJakartaVersion(ver, 2.2, 2.1, 2.0);
        }

        // json-api || jakarta.json - 2.0, 2.1, 2.1
        if (name.contains("json-api") || name.equals("jakarta.json")) {
            return mapToJakartaVersion(ver, 2.1, 2.1, 2.0);
        }

        // json.bind-api || jakarta.json.bind - 2.0, 3.0, 3.0
        if (name.contains("json.bind-api") || name.equals("jakarta.json.bind")) {
            return mapToJakartaVersion(ver, 3.0, 3.0, 2.0);
        }

        // annotation-api || jakarta.annotation - 2.0, 2.1, 3.0
        if (name.contains("annotation-api") || name.equals("jakarta.annotation")) {
            return mapToJakartaVersion(ver, 3.0, 2.1, 2.0);
        }

        // ejb-api || jakarta.ejb - 4.0
        if (name.contains("ejb-api") || name.equals("jakarta.ejb")) {
            return mapToJakartaVersion(ver, 4.0, 4.0, 4.0);
        }

        // transaction-api || jakarta.transaction - 2.0
        if (name.contains("transaction-api") || name.equals("jakarta.transaction")) {
            return mapToJakartaVersion(ver, 2.0, 2.0, 2.0);
        }

        // --- PERSISTENCE ---
        if (name.contains("persistence-api") || name.contains("jakarta.persistence")) {
            return mapToJakartaVersion(ver, 3.2, 3.1, 3.0);
        }

        // validation-api || jakarta.validation - 3.0, 3.0, 3.1
        if (name.contains("validation-api") || name.contains("jakarta.validation")) {
            return mapToJakartaVersion(ver, 3.1, 3.0, 3.0);
        }

        // interceptor-api || jakarta.interceptor - 2.0, 2.1, 2.2
        if (name.contains("interceptor-api") || name.contains("jakarta.interceptor")) {
            return mapToJakartaVersion(ver, 2.2, 2.1, 2.0);
        }

        // enterprise.cdi-api || jakarta.enterprise - 3.0, 4.0, 4.1
        if (name.contains("enterprise.cdi-api") || name.contains("jakarta.enterprise")) {
            return mapToJakartaVersion(ver, 4.1, 4.0, 3.0);
        }

        // inject-api || jakarta.inject - 2.0
        if (name.contains("inject-api") || name.contains("jakarta.inject")) {
            return mapToJakartaVersion(ver, 2.0, 2.0, 2.0);
        }

        // security.enterprise-api || jakarta.security.enterprise - 2.0, 3.0, 4.0
        if (name.contains("security.enterprise-api") || name.contains("jakarta.security.enterprise")) {
            return mapToJakartaVersion(ver, 4.0, 3.0, 2.0);
        }

        // data-api || jakarta.data - 1.0(11)
        if (name.contains("data-api") || name.contains("jakarta.data")) {
            return mapToJakartaVersion(ver, 1.0, 0.0, 0.0);
        }

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
