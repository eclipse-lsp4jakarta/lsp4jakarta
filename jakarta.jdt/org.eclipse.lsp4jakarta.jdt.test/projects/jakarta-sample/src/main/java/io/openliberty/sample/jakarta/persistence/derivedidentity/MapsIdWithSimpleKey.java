package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * INVALID: @MapsId specifies a value but entity has only a simple @Id, not a composite key.
 * Expects: MapsIdRequiresCompositeKey on field "employee"
 */
@Entity
public class MapsIdWithSimpleKey {

    @Id
    private long id;

    @MapsId("empId")
    @ManyToOne
    private Employee employee;
}
