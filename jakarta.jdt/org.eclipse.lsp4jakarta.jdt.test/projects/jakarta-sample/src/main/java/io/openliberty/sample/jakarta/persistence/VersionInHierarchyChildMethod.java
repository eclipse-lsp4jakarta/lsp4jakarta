package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;

@Entity
public class VersionInHierarchyChildMethod extends VersionInHierarchyParentMethod {
    
    private int childVersion;
    private String description;
    
    public VersionInHierarchyChildMethod() {
    }
    
    @Version
    public int getChildVersion() {
        return childVersion;
    }
    
    public void setChildVersion(int childVersion) {
        this.childVersion = childVersion;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

