package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public class VersionInHierarchyParent {
    
    @Version
    private int version; 
    
    private String name;
    
    public VersionInHierarchyParent() {
    }
}

