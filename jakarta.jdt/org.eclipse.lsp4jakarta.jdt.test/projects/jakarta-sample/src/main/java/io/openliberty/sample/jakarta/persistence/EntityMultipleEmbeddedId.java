package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class EntityMultipleEmbeddedId {

    @EmbeddedId
    private CompositeKey id1;

    @EmbeddedId
    private CompositeKey id2;

    public EntityMultipleEmbeddedId() {
    }
}
