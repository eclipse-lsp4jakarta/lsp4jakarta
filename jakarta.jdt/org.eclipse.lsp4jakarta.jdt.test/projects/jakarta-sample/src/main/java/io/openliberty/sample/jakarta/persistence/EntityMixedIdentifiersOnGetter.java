package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EntityMixedIdentifiersOnGetter {

    private Long id;
    private CompositeKey compositeId;

    public EntityMixedIdentifiersOnGetter() {
    }

    @Id
    public Long getId() {
        return id;
    }

    @EmbeddedId
    public CompositeKey getCompositeId() {
        return compositeId;
    }
}
