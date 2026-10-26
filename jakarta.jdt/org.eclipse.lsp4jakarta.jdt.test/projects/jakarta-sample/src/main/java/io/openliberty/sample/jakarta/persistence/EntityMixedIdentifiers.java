package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EntityMixedIdentifiers {

    @Id
    private Long id;

    @EmbeddedId
    private CompositeKey compositeId;

    public EntityMixedIdentifiers() {
    }
}
