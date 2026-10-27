package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * INVALID: @MapsId("empId") references ProjectId.empId (long) but
 * the parent entity Department has a String @Id — type mismatch.
 * Expects: MapsIdValueTypeMismatch on field "department"
 */
@Entity
public class MapsIdValueTypeMismatch {

    @EmbeddedId
    private ProjectId id;

    @MapsId("empId")
    @ManyToOne
    private Department department;
}
