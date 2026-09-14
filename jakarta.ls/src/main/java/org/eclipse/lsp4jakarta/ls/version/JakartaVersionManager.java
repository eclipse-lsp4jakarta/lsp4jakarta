package org.eclipse.lsp4jakarta.ls.version;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JakartaVersionManager {

    private static volatile JakartaVersionManager instance;

    // HashMap to store project versions (key: project name, value: Jakarta version)
    private Map<String, JakartaVersion> projectVersionMap;

    private JakartaVersionManager() {
        projectVersionMap = new HashMap<>();
    }

    // Public method to get the singleton instance (thread-safe)
    public static JakartaVersionManager getInstance() {
        if (instance == null) {
            synchronized (JakartaVersionManager.class) {
                if (instance == null) {
                    instance = new JakartaVersionManager();
                }
            }
        }
        return instance;
    }

    public void setVersion(String projectName, JakartaVersion version) {
        projectVersionMap.put(projectName, version);
    }

    public JakartaVersion getVersion(String projectName, List<String> entries) {
        if (!hasVersion(projectName)) {
            this.setVersion(projectName, JakartaVersionFinder.analyzeClasspath(entries));
        }
        return projectVersionMap.get(projectName);
    }

    public JakartaVersion getVersion(String projectName) {

        return projectVersionMap.get(projectName);
    }

    public boolean hasVersion(String projectName) {
        return projectVersionMap.containsKey(projectName);
    }

    public void removeVersion(String projectName) {
        projectVersionMap.remove(projectName);
    }

    public Map<String, JakartaVersion> getAllVersions() {
        return new HashMap<>(projectVersionMap);
    }

    public void clearVersions() {
        projectVersionMap.clear();
    }

    public int getVersionCount() {
        return projectVersionMap.size();
    }
}
