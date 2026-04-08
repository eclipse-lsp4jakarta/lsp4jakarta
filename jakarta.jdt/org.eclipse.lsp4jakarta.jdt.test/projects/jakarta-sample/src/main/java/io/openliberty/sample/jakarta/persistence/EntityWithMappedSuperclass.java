package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;

@Entity
public class EntityWithMappedSuperclass extends BaseMappedSuperclass {
    
    private String name;
    
    public EntityWithMappedSuperclass() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
