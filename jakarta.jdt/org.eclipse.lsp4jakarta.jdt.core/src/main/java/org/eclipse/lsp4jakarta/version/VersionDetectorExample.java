package org.eclipse.lsp4jakarta.version;

import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Example usage of the Jakarta version detection system.
 * This class demonstrates how to use the different detection strategies.
 */
public class VersionDetectorExample {

    /**
     * Example demonstrating the usage of different detection strategies.
     *
     * @param entries The classpath entries to analyze
     */
    public static void demonstrateUsage(IClasspathEntry[] entries) {
        System.out.println("=== Jakarta Version Detection Examples ===\n");

        // Example 1: Using default strategy (filename parsing - backward compatible)
        System.out.println("1. Default Strategy (Filename Parsing):");
        List<JakartaVersion> version1 = JakartaVersionFinder.analyzeClasspath(entries);
        version1.forEach(v -> System.out.println("Detected: " + v.getLabel()));
        System.out.println();

        // Example 2: Using manifest inspection
        System.out.println("2. Manifest Inspection Strategy:");
        List<JakartaVersion> version2 = JakartaVersionFinder.analyzeClasspath(entries,
                                                                              JakartaVersionFinder.DetectionStrategy.MANIFEST);
        version2.forEach(v -> System.out.println("Detected: " + v.getLabel()));
        System.out.println();

        // Example 3: Try manifest first, fallback to filename
        System.out.println("3. Manifest Then Filename Strategy:");
        List<JakartaVersion> version3 = JakartaVersionFinder.analyzeClasspath(entries,
                                                                              JakartaVersionFinder.DetectionStrategy.MANIFEST_THEN_FILENAME);
        version3.forEach(v -> System.out.println("Detected: " + v.getLabel()));
        System.out.println();

        // Example 4: Try filename first, fallback to manifest
        System.out.println("4. Filename Then Manifest Strategy:");
        List<JakartaVersion> version4 = JakartaVersionFinder.analyzeClasspath(entries,
                                                                              JakartaVersionFinder.DetectionStrategy.FILENAME_THEN_MANIFEST);
        version4.forEach(v -> System.out.println("Detected: " + v.getLabel()));
        System.out.println();

        // Example 5: Using URI-based detection
        System.out.println("5. URI-based Detection (default strategy):");
        // List<JakartaVersion> version5 = JakartaVersionFinder.analyzeClasspath("file:///path/to/file.java");
        // version5.forEach(v -> System.out.println("Detected: " + v.getLabel()));

        // Example 6: Using URI-based detection with custom strategy
        System.out.println("6. URI-based Detection (manifest strategy):");
        // List<JakartaVersion> version6 = JakartaVersionFinder.analyzeClasspath(
        //     "file:///path/to/file.java",
        //     JakartaVersionFinder.DetectionStrategy.MANIFEST);
        // version6.forEach(v -> System.out.println("Detected: " + v.getLabel()));
    }

    /**
     * Example of using individual detectors directly.
     *
     * @param entries The classpath entries to analyze
     */
    public static void demonstrateDirectDetectorUsage(IClasspathEntry[] entries) {
        System.out.println("=== Direct Detector Usage ===\n");

        // Using filename detector directly
        JarFilenameVersionDetector filenameDetector = new JarFilenameVersionDetector();
        List<JakartaVersion> filenameVersions = filenameDetector.detectVersion(entries);
        filenameVersions.forEach(v -> System.out.println("Filename Detector: " + v.getLabel()));
        System.out.println();

        // Using manifest detector directly
        JarManifestVersionDetector manifestDetector = new JarManifestVersionDetector();
        List<JakartaVersion> manifestVersions = manifestDetector.detectVersion(entries);
        manifestVersions.forEach(v -> System.out.println("Manifest Detector: " + v.getLabel()));
        System.out.println();
    }
}

// Made with Bob
