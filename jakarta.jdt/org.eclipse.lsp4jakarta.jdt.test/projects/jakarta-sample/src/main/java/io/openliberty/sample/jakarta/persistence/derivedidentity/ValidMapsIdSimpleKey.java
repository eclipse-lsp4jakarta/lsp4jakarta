package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

/**
 * VALID (spec section 2.4.1.1, Example 4b): value-less @MapsId with a simple @Id.
 * Expects: no diagnostics
 */
@Entity
public class ValidMapsIdSimpleKey {

    @Id
    private long id;

    @MapsId
    @OneToOne
    private Employee employee;
}
