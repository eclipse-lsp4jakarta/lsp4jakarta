package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * INVALID: @MapsId("active") binds to GetterAccessKey.isActive() (boolean), but
 * Department's @Id is String.
 * Expects: MapsIdValueTypeMismatch on field "department"
 */
@Entity
public class MapsIdIsPropertyAccessTypeMismatch {

    @EmbeddedId
    private GetterAccessKey id;

    @MapsId("active")
    @ManyToOne
    private Department department;
}
