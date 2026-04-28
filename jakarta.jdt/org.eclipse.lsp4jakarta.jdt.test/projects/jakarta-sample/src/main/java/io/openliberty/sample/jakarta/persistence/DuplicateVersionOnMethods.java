package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;

@Entity
public class DuplicateVersionOnMethods {
    
    private int version1;
    private int version2;
    private String name;
    
    public DuplicateVersionOnMethods() {
    }
    
    @Version
    public int getVersion1() {
        return version1;
    }
    
    public void setVersion1(int version1) {
        this.version1 = version1;
    }
    
    @Version
    public int getVersion2() {
        return version2;
    }
    
    public void setVersion2(int version2) {
        this.version2 = version2;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}

// Made with Bob
