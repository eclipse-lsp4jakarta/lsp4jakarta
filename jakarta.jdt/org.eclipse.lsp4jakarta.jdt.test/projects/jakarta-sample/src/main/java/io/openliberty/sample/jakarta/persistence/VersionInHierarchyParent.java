package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;

@Entity
public class VersionInHierarchyParent {
    
    @Version
    private int version;
    
    private String name;
    
    public VersionInHierarchyParent() {
    }
}

// Made with Bob
