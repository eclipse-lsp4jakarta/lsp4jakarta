package org.eclipse.lsp4jakarta.version;

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
        JakartaVersion version1 = JakartaVersionFinder.analyzeClasspath(entries);
        System.out.println("Detected: " + version1.getLabel() + "\n");

        // Example 2: Using manifest inspection
        System.out.println("2. Manifest Inspection Strategy:");
        JakartaVersion version2 = JakartaVersionFinder.analyzeClasspath(entries,
                                                                        JakartaVersionFinder.DetectionStrategy.MANIFEST);
        System.out.println("Detected: " + version2.getLabel() + "\n");

        // Example 3: Try manifest first, fallback to filename
        System.out.println("3. Manifest Then Filename Strategy:");
        JakartaVersion version3 = JakartaVersionFinder.analyzeClasspath(entries,
                                                                        JakartaVersionFinder.DetectionStrategy.MANIFEST_THEN_FILENAME);
        System.out.println("Detected: " + version3.getLabel() + "\n");

        // Example 4: Try filename first, fallback to manifest
        System.out.println("4. Filename Then Manifest Strategy:");
        JakartaVersion version4 = JakartaVersionFinder.analyzeClasspath(entries,
                                                                        JakartaVersionFinder.DetectionStrategy.FILENAME_THEN_MANIFEST);
        System.out.println("Detected: " + version4.getLabel() + "\n");

        // Example 5: Using URI-based detection
        System.out.println("5. URI-based Detection (default strategy):");
        // JakartaVersion version5 = JakartaVersionFinder.analyzeClasspath("file:///path/to/file.java");
        // System.out.println("Detected: " + version5.getLabel() + "\n");

        // Example 6: Using URI-based detection with custom strategy
        System.out.println("6. URI-based Detection (manifest strategy):");
        // JakartaVersion version6 = JakartaVersionFinder.analyzeClasspath(
        //     "file:///path/to/file.java",
        //     JakartaVersionFinder.DetectionStrategy.MANIFEST);
        // System.out.println("Detected: " + version6.getLabel() + "\n");
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
        JakartaVersion filenameVersion = filenameDetector.detectVersion(entries);
        System.out.println("Filename Detector: " + filenameVersion.getLabel() + "\n");

        // Using manifest detector directly
        JarManifestVersionDetector manifestDetector = new JarManifestVersionDetector();
        JakartaVersion manifestVersion = manifestDetector.detectVersion(entries);
        System.out.println("Manifest Detector: " + manifestVersion.getLabel() + "\n");
    }
}

// Made with Bob
