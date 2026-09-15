package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EntityMixedIdentifiersGetterAndField {

    @EmbeddedId
    private CompositeKey compositeId;

    private Long id;

    public EntityMixedIdentifiersGetterAndField() {
    }

    @Id
    public Long getId() {
        return id;
    }
}
