package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;

@Entity
public class EntityWithEmbeddedIdOnGetter {
    
    private CompositeKey id;
    private String name;
    
    public EntityWithEmbeddedIdOnGetter() {
    }
    
    @EmbeddedId
    public CompositeKey getId() {
        return id;
    }
    
    public void setId(CompositeKey id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
