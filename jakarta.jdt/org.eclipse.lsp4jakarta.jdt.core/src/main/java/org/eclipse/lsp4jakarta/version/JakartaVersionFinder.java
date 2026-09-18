package org.eclipse.lsp4jakarta.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Main facade for Jakarta EE version detection.
 * Uses multiple detection strategies to identify the Jakarta EE version.
 */
public class JakartaVersionFinder {

    /**
     * Detection strategy enum to specify which detection method to use.
     */
    public enum DetectionStrategy {
        /** Use JAR filename parsing (default, backward compatible) */
        FILENAME,
        /** Use JAR manifest inspection */
        MANIFEST,
        /** Use class signature detection */
        CLASS_SIGNATURE,
        /** Try manifest first, fallback to filename */
        MANIFEST_THEN_FILENAME,
        /** Try filename first, fallback to manifest */
        FILENAME_THEN_MANIFEST,
        /** Try class signature first, fallback to manifest, then filename */
        CLASS_SIGNATURE_THEN_MANIFEST_THEN_FILENAME
    }

    private static final JarFilenameVersionDetector filenameDetector = new JarFilenameVersionDetector();
    private static final JarManifestVersionDetector manifestDetector = new JarManifestVersionDetector();
    private static final ClassSignatureVersionDetector classSignatureDetector = new ClassSignatureVersionDetector();

    /**
     * Analyzes classpath using the default detection strategy (filename parsing).
     * This method maintains backward compatibility with existing code.
     *
     * @param entries The classpath entries to analyze
     * @return The detected Jakarta version
     */
    public static List<JakartaVersion> analyzeClasspath(IClasspathEntry[] entries) {
        return analyzeClasspath(entries, DetectionStrategy.MANIFEST_THEN_FILENAME);
    }

    public static List<JakartaVersion> analyzeClasspath(IClasspathEntry[] entries, IJavaProject javaProject) {
        return analyzeClasspath(entries, javaProject, DetectionStrategy.MANIFEST_THEN_FILENAME);
    }

    /**
     * Analyzes classpath using the specified detection strategy.
     *
     * @param entries The classpath entries to analyze
     * @param strategy The detection strategy to use
     * @return The detected Jakarta version
     */
    public static List<JakartaVersion> analyzeClasspath(IClasspathEntry[] entries, DetectionStrategy strategy) {
        return analyzeClasspath(entries, null, strategy);
    }

    /**
     * Analyzes classpath using the specified detection strategy with Java project context.
     *
     * @param entries The classpath entries to analyze
     * @param javaProject The Java project (required for CLASS_SIGNATURE strategy)
     * @param strategy The detection strategy to use
     * @return The detected Jakarta version
     */
    public static List<JakartaVersion> analyzeClasspath(IClasspathEntry[] entries, IJavaProject javaProject, DetectionStrategy strategy) {
        List<JakartaVersion> detectedVersions = new ArrayList<>();

        switch (strategy) {
            case MANIFEST:
                detectedVersions = manifestDetector.detectVersion(entries);
                break;

            case FILENAME:
                detectedVersions = filenameDetector.detectVersion(entries);
                break;

            case CLASS_SIGNATURE:
                if (javaProject != null) {
                    detectedVersions = classSignatureDetector.detectVersion(javaProject);
                } else {
                    System.out.println("CLASS_SIGNATURE strategy requires IJavaProject, falling back to MANIFEST");
                    detectedVersions = manifestDetector.detectVersion(entries);
                }
                break;

            case MANIFEST_THEN_FILENAME:
                detectedVersions = manifestDetector.detectVersion(entries);
                if (isUnknownVersion(detectedVersions)) {
                    System.out.println("Manifest detection inconclusive, trying filename detection...");
                    detectedVersions = filenameDetector.detectVersion(entries);
                }
                break;

            case FILENAME_THEN_MANIFEST:
                detectedVersions = filenameDetector.detectVersion(entries);
                if (isUnknownVersion(detectedVersions)) {
                    System.out.println("Filename detection inconclusive, trying manifest detection...");
                    detectedVersions = manifestDetector.detectVersion(entries);
                }
                break;

            case CLASS_SIGNATURE_THEN_MANIFEST_THEN_FILENAME:
                if (javaProject != null) {
                    detectedVersions = classSignatureDetector.detectVersion(javaProject);
                }
                if (isUnknownVersion(detectedVersions)) {
                    System.out.println("Class signature detection inconclusive, trying manifest detection...");
                    detectedVersions = manifestDetector.detectVersion(entries);
                }
                if (isUnknownVersion(detectedVersions)) {
                    System.out.println("Manifest detection inconclusive, trying filename detection...");
                    detectedVersions = filenameDetector.detectVersion(entries);
                }
                break;

            default:
                detectedVersions = filenameDetector.detectVersion(entries);
                break;
        }

        // Fallback to JEE9 if version is still unknown
        if (isUnknownVersion(detectedVersions)) {
            System.out.println("UNKNOWN version: fall back to JEE9");
            detectedVersions.add(JakartaVersion.EE_9);
        }

        return detectedVersions;
    }

    /**
     * Analyzes classpath for a given URI using the default detection strategy.
     * This method maintains backward compatibility with existing code.
     *
     * @param uri The URI of the compilation unit
     * @return The detected Jakarta version
     */
    public static List<JakartaVersion> analyzeClasspath(String uri) {
        return analyzeClasspath(uri, DetectionStrategy.FILENAME);
    }

    /**
     * Analyzes classpath for a given URI using the specified detection strategy.
     *
     * @param uri The URI of the compilation unit
     * @param strategy The detection strategy to use
     * @return The detected Jakarta version
     */
    public static List<JakartaVersion> analyzeClasspath(String uri, DetectionStrategy strategy) {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        IJavaProject javaProject = unit.getJavaProject();
        IClasspathEntry[] entries = null;

        try {
            entries = javaProject.getResolvedClasspath(true);
        } catch (JavaModelException e) {
            e.printStackTrace();
            List<JakartaVersion> defaultVersion = new ArrayList<>();
            defaultVersion.add(JakartaVersion.EE_9);
            return defaultVersion;// Fallback
        }

        return analyzeClasspath(entries, javaProject, strategy);
    }

    /**
     * Returns a static list of all known Jakarta EE versions (highest first).
     *
     * @return all known Jakarta EE versions as a list
     */
    public static List<JakartaVersion> getAllKnownVersions() {
        return Arrays.asList(JakartaVersion.EE_11, JakartaVersion.EE_10, JakartaVersion.EE_9, JakartaVersion.EE_8);
    }

    private static boolean isUnknownVersion(List<JakartaVersion> detectedVersions) {
        return detectedVersions.isEmpty()
               || detectedVersions.stream().allMatch(v -> v == JakartaVersion.UNKNOWN);
    }
}
