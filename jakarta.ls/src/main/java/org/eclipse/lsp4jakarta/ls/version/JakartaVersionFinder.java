package org.eclipse.lsp4jakarta.ls.version;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JakartaVersionFinder {

    public static JakartaVersion analyzeClasspath(List<String> entries) {

        // Regex to match JAR names and versions: matches "name-version.jar"
        Pattern jarPattern = Pattern.compile("([^/\\\\]+)-([0-9\\.]+[^/\\\\]*)\\.jar$");

        JakartaVersion detectedVersion = JakartaVersion.UNKNOWN;

        for (String line : entries) {
            System.out.println("Classpath entry: " + line);

            if (line.isEmpty() || !line.endsWith(".jar"))
                continue;

            Matcher matcher = jarPattern.matcher(line);
            if (matcher.find()) {
                String artifactName = matcher.group(1);
                String version = matcher.group(2);

                // Priority 1: Check for the Jakarta EE Platform base Jar
                if (artifactName.contains("jakartaee-api") || artifactName.contains("jakartaee-web-api")
                    || artifactName.contains("jakartaee-core-api")) {
                    JakartaVersion v = getBaseVersion(version);
                    if (v.getLevel() > detectedVersion.getLevel()) {
                        detectedVersion = v;

                    }
                }

                // Prority 2: check module version
                JakartaVersion moduleVersion = getModuleVersion(artifactName, version);
                if (moduleVersion.getLevel() > detectedVersion.getLevel()) {
                    detectedVersion = moduleVersion;

                }
            }
        }

        if (detectedVersion.equals(JakartaVersion.UNKNOWN)) {
            System.out.println("UNKNOWN version : fall back to JEE9");
            detectedVersion = JakartaVersion.EE_9;
        }

        System.out.println("------------------------------------------------------------------");
        System.out.println("Identified Platform Version: " + detectedVersion.getLabel() + "-" + detectedVersion.getLevel());
        System.out.println("------------------------------------------------------------------");

        return detectedVersion;
    }

    private static JakartaVersion getBaseVersion(String version) {
        if (version.startsWith("11."))
            return JakartaVersion.EE_11;
        if (version.startsWith("10."))
            return JakartaVersion.EE_10;
        if (version.startsWith("9."))
            return JakartaVersion.EE_9;

        return JakartaVersion.UNKNOWN;
    }

    private static JakartaVersion getModuleVersion(String name, String version) {
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

    private static double parseVersionDouble(String version) {
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

    private static JakartaVersion mapToJakartaVersion(double ver, double v11, double v10, double v9) {
        if (ver >= v11)
            return JakartaVersion.EE_11;
        if (ver >= v10)
            return JakartaVersion.EE_10;
        if (ver >= v9)
            return JakartaVersion.EE_9;

        return JakartaVersion.UNKNOWN;
    }
}
