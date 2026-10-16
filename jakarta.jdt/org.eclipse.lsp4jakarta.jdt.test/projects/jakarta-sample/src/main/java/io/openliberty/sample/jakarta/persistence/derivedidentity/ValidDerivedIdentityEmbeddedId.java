package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * VALID: @MapsId("empId") on @ManyToOne with @EmbeddedId that has empId field.
 * No diagnostic expected.
 */
@Entity
public class ValidDerivedIdentityEmbeddedId {

    @EmbeddedId
    private ProjectId id;

    @MapsId("empId")
    @ManyToOne
    private Employee employee;
}
