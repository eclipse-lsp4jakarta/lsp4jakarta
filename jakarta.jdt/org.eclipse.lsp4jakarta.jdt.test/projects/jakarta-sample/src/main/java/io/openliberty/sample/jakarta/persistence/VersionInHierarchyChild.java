package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;

@Entity
public class VersionInHierarchyChild extends VersionInHierarchyParent {
    
    @Version
    private int childVersion;
    
    private String description;
    
    public VersionInHierarchyChild() {
    }
}

