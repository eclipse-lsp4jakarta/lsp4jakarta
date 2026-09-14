package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import jakarta.persistence.Id;

@Entity
public class VersionInHierarchyChild extends VersionInHierarchyParent {
    
    @Version
    private int childVersion;
    
    private String description;
    
    @Id
    private int id;
    
    public VersionInHierarchyChild() {
    }
}

