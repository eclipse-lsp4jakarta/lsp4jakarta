package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * VALID: @MapsId("empId") binds to GetterAccessKey.getEmpId() (long), which
 * matches Employee's @Id (long).
 */
@Entity
public class ValidMapsIdPropertyAccess {

    @EmbeddedId
    private GetterAccessKey id;

    @MapsId("empId")
    @ManyToOne
    private Employee employee;
}
