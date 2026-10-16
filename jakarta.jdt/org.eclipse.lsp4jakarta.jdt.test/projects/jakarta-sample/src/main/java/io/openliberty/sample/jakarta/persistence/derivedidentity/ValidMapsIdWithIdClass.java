package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * VALID: @MapsId("empId") on an entity whose composite key is declared via @IdClass.
 * Expects: no diagnostics (in particular, no MapsIdRequiresCompositeKey)
 */
@Entity
@IdClass(ProjectId.class)
public class ValidMapsIdWithIdClass {

    @Id
    private long empId;

    @Id
    private long projectId;

    @MapsId("empId")
    @ManyToOne
    private Employee employee;
}
