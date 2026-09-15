package org.eclipse.lsp4jakarta.version;

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
    public static JakartaVersion analyzeClasspath(IClasspathEntry[] entries) {
        return analyzeClasspath(entries, DetectionStrategy.MANIFEST_THEN_FILENAME);
    }
    
    public static JakartaVersion analyzeClasspath(IClasspathEntry[] entries, IJavaProject javaProject) {
        return analyzeClasspath(entries,javaProject, DetectionStrategy.MANIFEST_THEN_FILENAME);
    }

    /**
     * Analyzes classpath using the specified detection strategy.
     *
     * @param entries The classpath entries to analyze
     * @param strategy The detection strategy to use
     * @return The detected Jakarta version
     */
    public static JakartaVersion analyzeClasspath(IClasspathEntry[] entries, DetectionStrategy strategy) {
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
    public static JakartaVersion analyzeClasspath(IClasspathEntry[] entries, IJavaProject javaProject, DetectionStrategy strategy) {
        JakartaVersion detectedVersion = JakartaVersion.UNKNOWN;

        switch (strategy) {
            case MANIFEST:
                detectedVersion = manifestDetector.detectVersion(entries);
                break;

            case FILENAME:
                detectedVersion = filenameDetector.detectVersion(entries);
                break;

            case CLASS_SIGNATURE:
                if (javaProject != null) {
                    detectedVersion = classSignatureDetector.detectVersion(javaProject);
                } else {
                    System.out.println("CLASS_SIGNATURE strategy requires IJavaProject, falling back to MANIFEST");
                    detectedVersion = manifestDetector.detectVersion(entries);
                }
                break;

            case MANIFEST_THEN_FILENAME:
                detectedVersion = manifestDetector.detectVersion(entries);
                if (detectedVersion == JakartaVersion.UNKNOWN) {
                    System.out.println("Manifest detection inconclusive, trying filename detection...");
                    detectedVersion = filenameDetector.detectVersion(entries);
                }
                break;

            case FILENAME_THEN_MANIFEST:
                detectedVersion = filenameDetector.detectVersion(entries);
                if (detectedVersion == JakartaVersion.UNKNOWN) {
                    System.out.println("Filename detection inconclusive, trying manifest detection...");
                    detectedVersion = manifestDetector.detectVersion(entries);
                }
                break;

            case CLASS_SIGNATURE_THEN_MANIFEST_THEN_FILENAME:
                if (javaProject != null) {
                    detectedVersion = classSignatureDetector.detectVersion(javaProject);
                }
                if (detectedVersion == JakartaVersion.UNKNOWN) {
                    System.out.println("Class signature detection inconclusive, trying manifest detection...");
                    detectedVersion = manifestDetector.detectVersion(entries);
                }
                if (detectedVersion == JakartaVersion.UNKNOWN) {
                    System.out.println("Manifest detection inconclusive, trying filename detection...");
                    detectedVersion = filenameDetector.detectVersion(entries);
                }
                break;

            default:
                detectedVersion = filenameDetector.detectVersion(entries);
                break;
        }

        // Fallback to JEE9 if version is still unknown
        if (detectedVersion == JakartaVersion.UNKNOWN) {
            System.out.println("UNKNOWN version: fall back to JEE9");
            detectedVersion = JakartaVersion.EE_9;
        }

        return detectedVersion;
    }

    /**
     * Analyzes classpath for a given URI using the default detection strategy.
     * This method maintains backward compatibility with existing code.
     *
     * @param uri The URI of the compilation unit
     * @return The detected Jakarta version
     */
    public static JakartaVersion analyzeClasspath(String uri) {
        return analyzeClasspath(uri, DetectionStrategy.FILENAME);
    }

    /**
     * Analyzes classpath for a given URI using the specified detection strategy.
     *
     * @param uri The URI of the compilation unit
     * @param strategy The detection strategy to use
     * @return The detected Jakarta version
     */
    public static JakartaVersion analyzeClasspath(String uri, DetectionStrategy strategy) {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        IJavaProject javaProject = unit.getJavaProject();
        IClasspathEntry[] entries = null;

        try {
            entries = javaProject.getResolvedClasspath(true);
        } catch (JavaModelException e) {
            e.printStackTrace();
            return JakartaVersion.EE_9; // Fallback
        }

        return analyzeClasspath(entries, javaProject, strategy);
    }
}
