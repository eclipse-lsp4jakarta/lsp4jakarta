package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * INVALID: the parent's primary key is the composite type ProjectId, but the
 * @MapsId attribute wrongKey is a String.
 * Expects: MapsIdValueTypeMismatch on field "parent"
 */
@Entity
public class MapsIdCompositeParentTypeMismatch {

    @EmbeddedId
    private ChildKey id;

    @MapsId("wrongKey")
    @ManyToOne
    private ParentWithEmbeddedId parent;
}
