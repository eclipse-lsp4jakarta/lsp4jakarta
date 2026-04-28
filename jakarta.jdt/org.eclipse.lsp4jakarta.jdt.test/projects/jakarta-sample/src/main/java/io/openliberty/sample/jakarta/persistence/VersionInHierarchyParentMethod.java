package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;

@Entity
public class VersionInHierarchyParentMethod {
    
    private int version;
    private String name;
    
    public VersionInHierarchyParentMethod() {
    }
    
    @Version
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}

// Made with Bob
