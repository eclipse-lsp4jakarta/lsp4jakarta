package org.eclipse.lsp4jakarta.version;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Detects Jakarta EE version by checking for version-specific class signatures.
 * This strategy analyzes the actual API capabilities available in the classpath
 * by checking for the presence of version-specific classes and packages across
 * all Jakarta EE modules.
 *
 * This approach is independent of JAR naming, manifest metadata, or build tools,
 * making it the most reliable detection method for any project type.
 */
public class ClassSignatureVersionDetector {

    /**
     * Analyzes Jakarta version based on class signatures.
     * Checks for version-specific classes across all Jakarta modules.
     *
     * @param javaProject The Java project for class resolution
     * @return The detected Jakarta version based on available classes
     */
    public JakartaVersion detectVersion(IJavaProject javaProject) {
        Map<JakartaVersion, Integer> versionScores = new HashMap<>();
        versionScores.put(JakartaVersion.EE_11, 0);
        versionScores.put(JakartaVersion.EE_10, 0);
        versionScores.put(JakartaVersion.EE_9, 0);

        Set<String> detectedFeatures = new HashSet<>();

        System.out.println("Class Signature Analysis (All Modules)-----------");

        // Check all modules for each version
        checkServletVersion(javaProject, versionScores, detectedFeatures);
        checkPersistenceVersion(javaProject, versionScores, detectedFeatures);
        checkCDIVersion(javaProject, versionScores, detectedFeatures);
        checkFacesVersion(javaProject, versionScores, detectedFeatures);
        checkRESTVersion(javaProject, versionScores, detectedFeatures);
        checkWebSocketVersion(javaProject, versionScores, detectedFeatures);
        checkJSONVersion(javaProject, versionScores, detectedFeatures);
        checkJSONBVersion(javaProject, versionScores, detectedFeatures);
        checkAnnotationVersion(javaProject, versionScores, detectedFeatures);
        checkEJBVersion(javaProject, versionScores, detectedFeatures);
        checkTransactionVersion(javaProject, versionScores, detectedFeatures);
        checkValidationVersion(javaProject, versionScores, detectedFeatures);
        checkInterceptorVersion(javaProject, versionScores, detectedFeatures);
        checkInjectVersion(javaProject, versionScores, detectedFeatures);
        checkSecurityVersion(javaProject, versionScores, detectedFeatures);
        checkDataVersion(javaProject, versionScores, detectedFeatures);

        // Determine version based on highest score
        JakartaVersion detectedVersion = JakartaVersion.UNKNOWN;
        int maxScore = 0;
        for (Map.Entry<JakartaVersion, Integer> entry : versionScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                detectedVersion = entry.getKey();
            }
        }

        System.out.println("Detected Features:");
        detectedFeatures.forEach(feature -> System.out.println("  - " + feature));
        System.out.println("Version Scores: EE11=" + versionScores.get(JakartaVersion.EE_11) +
                           ", EE10=" + versionScores.get(JakartaVersion.EE_10) +
                           ", EE9=" + versionScores.get(JakartaVersion.EE_9));
        System.out.println("------------------------------------------------------------------");
        System.out.println("Class Signature Detected Version: " + detectedVersion.getLabel() + "-" + detectedVersion.getLevel());
        System.out.println("------------------------------------------------------------------");

        return detectedVersion;
    }

    private void checkServletVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.servlet.ServletConnection")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("Servlet 6.1 (EE 11)");
        } else if (classExists(project, "jakarta.servlet.ServletContext") &&
                   methodExists(project, "jakarta.servlet.ServletContext", "getRequestCharacterEncoding")) {
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("Servlet 6.0 (EE 10)");
        } else if (classExists(project, "jakarta.servlet.Servlet")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Servlet 5.0 (EE 9)");
        }
    }

    private void checkPersistenceVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.persistence.criteria.CriteriaSelect")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("Persistence 3.2 (EE 11)");
        } else if (classExists(project, "jakarta.persistence.EntityManagerFactory") &&
                   methodExists(project, "jakarta.persistence.EntityManagerFactory", "runInTransaction")) {
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("Persistence 3.1 (EE 10)");
        } else if (classExists(project, "jakarta.persistence.Entity")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Persistence 3.0 (EE 9)");
        }
    }

    private void checkCDIVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("CDI 4.1 (EE 11)");
        } else if (classExists(project, "jakarta.enterprise.inject.build.compatible.spi.BeanInfo")) {
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("CDI 4.0 (EE 10)");
        } else if (classExists(project, "jakarta.enterprise.context.ApplicationScoped")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("CDI 3.0 (EE 9)");
        }
    }

    private void checkFacesVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.faces.annotation.View")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("Faces 4.1 (EE 11)");
        } else if (classExists(project, "jakarta.faces.push.Push")) {
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("Faces 4.0 (EE 10)");
        } else if (classExists(project, "jakarta.faces.component.UIComponent")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Faces 3.0 (EE 9)");
        }
    }

    private void checkRESTVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.ws.rs.SeBootstrap") ||
            classExists(project, "jakarta.ws.rs.core.EntityPart")) {
            // REST 3.1/4.0 share the same API - both in EE 10 and EE 11
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("REST 3.1/4.0 (EE 10/11)");
        } else if (classExists(project, "jakarta.ws.rs.Path")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("REST 3.0 (EE 9)");
        }
    }

    private void checkWebSocketVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.websocket.Session") &&
            methodExists(project, "jakarta.websocket.Session", "getRequestParameterMap")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("WebSocket 2.2 (EE 11)");
        } else if (classExists(project, "jakarta.websocket.ClientEndpointConfig")) {
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("WebSocket 2.1 (EE 10)");
        } else if (classExists(project, "jakarta.websocket.Endpoint")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("WebSocket 2.0 (EE 9)");
        }
    }

    private void checkJSONVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.json.JsonPatch")) {
            // JSON-P 2.1 is in both EE 10 and EE 11
            // We'll score both, but other modules will help differentiate
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("JSON-P 2.1 (EE 10/11)");
        } else if (classExists(project, "jakarta.json.JsonValue")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("JSON-P 2.0 (EE 9)");
        }
    }

    private void checkJSONBVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.json.bind.Jsonb")) {
            // JSON-B 3.0 is in both EE 10 and EE 11
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("JSON-B 3.0 (EE 10/11)");
        } else if (classExists(project, "jakarta.json.bind.JsonbBuilder")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("JSON-B 2.0 (EE 9)");
        }
    }

    private void checkAnnotationVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.annotation.ManagedBean")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("Annotation 3.0 (EE 11)");
        } else if (classExists(project, "jakarta.annotation.sql.DataSourceDefinitions")) {
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("Annotation 2.1 (EE 10)");
        } else if (classExists(project, "jakarta.annotation.PostConstruct")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Annotation 2.0 (EE 9)");
        }
    }

    private void checkEJBVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.ejb.Stateless")) {
            // EJB 4.0 is in EE 9, EE 10, and EE 11
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("EJB 4.0 (EE 9/10/11)");
        }
    }

    private void checkTransactionVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.transaction.Transactional")) {
            // Transaction 2.0 is in EE 9, EE 10, and EE 11
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Transaction 2.0 (EE 9/10/11)");
        }
    }

    private void checkValidationVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.validation.valueextraction.ValueExtractor")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("Validation 3.1 (EE 11)");
        } else if (classExists(project, "jakarta.validation.constraints.NotNull")) {
            // Validation 3.0 is in both EE 9 and EE 10
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Validation 3.0 (EE 9/10)");
        }
    }

    private void checkInterceptorVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.interceptor.InvocationContext") &&
            methodExists(project, "jakarta.interceptor.InvocationContext", "getInterceptorBindings")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("Interceptor 2.2 (EE 11)");
        } else if (classExists(project, "jakarta.interceptor.InvocationContext") &&
                   methodExists(project, "jakarta.interceptor.InvocationContext", "getContextData")) {
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("Interceptor 2.1 (EE 10)");
        } else if (classExists(project, "jakarta.interceptor.Interceptor")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Interceptor 2.0 (EE 9)");
        }
    }

    private void checkInjectVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.inject.Inject")) {
            // Inject 2.0 is in EE 9, EE 10, and EE 11
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Inject 2.0 (EE 9/10/11)");
        }
    }

    private void checkSecurityVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("Security 4.0 (EE 11)");
        } else if (classExists(project, "jakarta.security.enterprise.identitystore.IdentityStore")) {
            // Security 3.0 is in EE 10
            scores.put(JakartaVersion.EE_10, scores.get(JakartaVersion.EE_10) + 1);
            features.add("Security 3.0 (EE 10)");
        } else if (classExists(project, "jakarta.security.enterprise.SecurityContext")) {
            scores.put(JakartaVersion.EE_9, scores.get(JakartaVersion.EE_9) + 1);
            features.add("Security 2.0 (EE 9)");
        }
    }

    private void checkDataVersion(IJavaProject project, Map<JakartaVersion, Integer> scores, Set<String> features) {
        if (classExists(project, "jakarta.data.repository.Repository")) {
            scores.put(JakartaVersion.EE_11, scores.get(JakartaVersion.EE_11) + 1);
            features.add("Data 1.0 (EE 11)");
        }
    }

    /**
     * Checks if a class exists in the project's classpath.
     *
     * @param javaProject The Java project
     * @param fullyQualifiedClassName The fully qualified class name
     * @return true if the class exists
     */
    private boolean classExists(IJavaProject javaProject, String fullyQualifiedClassName) {
        try {
            IType type = javaProject.findType(fullyQualifiedClassName);
            return type != null && type.exists();
        } catch (JavaModelException e) {
            return false;
        }
    }

    /**
     * Checks if a method exists in a class.
     *
     * @param javaProject The Java project
     * @param fullyQualifiedClassName The fully qualified class name
     * @param methodName The method name to check
     * @return true if the method exists
     */
    private boolean methodExists(IJavaProject javaProject, String fullyQualifiedClassName, String methodName) {
        try {
            IType type = javaProject.findType(fullyQualifiedClassName);
            if (type != null && type.exists()) {
                // Check if any method with this name exists
                return type.getMethod(methodName, new String[0]).exists();
            }
            return false;
        } catch (JavaModelException e) {
            return false;
        }
    }
}

// Made with Bob
