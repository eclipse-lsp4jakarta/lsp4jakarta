package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EntityMixedIdentifiersFieldAndGetter {

    @Id
    private Long id;

    private CompositeKey compositeId;

    public EntityMixedIdentifiersFieldAndGetter() {
    }

    @EmbeddedId
    public CompositeKey getCompositeId() {
        return compositeId;
    }
}
