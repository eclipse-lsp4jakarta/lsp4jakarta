package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

/**
 * INVALID: @MapsId has no value, so the whole embedded id (ChildKey) must have the
 * same type as the parent's primary key (ProjectId), but it does not.
 * Expects: MapsIdPrimaryKeyTypeMismatch on field "parent"
 */
@Entity
public class MapsIdValuelessPkTypeMismatch {

    @EmbeddedId
    private ChildKey id;

    @MapsId
    @OneToOne
    private ParentWithEmbeddedId parent;
}
