package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * INVALID: @MapsId("nonExistent") — "nonExistent" is not an attribute in ProjectId.
 * Expects: MapsIdValueNotInEmbeddedIdClass on field "employee"
 */
@Entity
public class MapsIdValueNotInEmbeddedId {

    @EmbeddedId
    private ProjectId id;

    @MapsId("nonExistent")
    @ManyToOne
    private Employee employee;
}
