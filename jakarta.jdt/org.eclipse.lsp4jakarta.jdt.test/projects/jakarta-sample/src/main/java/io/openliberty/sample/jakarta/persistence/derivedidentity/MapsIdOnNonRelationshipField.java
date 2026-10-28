package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;

/**
 * INVALID: @MapsId applied to a plain @Id field — not a @OneToOne or @ManyToOne.
 * Expects: MapsIdOnInvalidRelationship on field "id"
 */
@Entity
public class MapsIdOnNonRelationshipField {

    @Id
    @MapsId
    private long id;
}
