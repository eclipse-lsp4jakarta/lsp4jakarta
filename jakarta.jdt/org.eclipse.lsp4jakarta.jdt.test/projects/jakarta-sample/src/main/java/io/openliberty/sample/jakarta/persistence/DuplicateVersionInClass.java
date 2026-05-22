package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import jakarta.persistence.Id;

@Entity
public class DuplicateVersionInClass {
    
    @Version
    private int version1;
    
    @Version
    private int version2;
    
    @Id
    private int id;
    
    private String name;
    
    public DuplicateVersionInClass() {
    }
}

