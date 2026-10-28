package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

/**
 * INVALID: @MapsId has no value, but the simple @Id (String) does not match
 * the parent's primary key type (long).
 * Expects: MapsIdPrimaryKeyTypeMismatch on field "employee"
 */
@Entity
public class MapsIdSimpleKeyTypeMismatch {

    @Id
    private String id;

    @MapsId
    @OneToOne
    private Employee employee;
}
