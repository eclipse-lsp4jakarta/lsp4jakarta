package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;

@Entity
public class DuplicateVersionInClass {
    
    @Version
    private int version1;
    
    @Version
    private int version2;
    
    private String name;
    
    public DuplicateVersionInClass() {
    }
}

// Made with Bob
