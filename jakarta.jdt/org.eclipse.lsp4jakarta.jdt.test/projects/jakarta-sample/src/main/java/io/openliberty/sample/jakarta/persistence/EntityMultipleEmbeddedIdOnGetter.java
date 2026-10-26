package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class EntityMultipleEmbeddedIdOnGetter {

    private CompositeKey id1;
    private CompositeKey id2;

    public EntityMultipleEmbeddedIdOnGetter() {
    }

    @EmbeddedId
    public CompositeKey getId1() {
        return id1;
    }

    @EmbeddedId
    public CompositeKey getId2() {
        return id2;
    }
}
