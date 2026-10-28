package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Parent entity with a simple long @Id.
 * Used as the parent in @MapsId("empId") relationship tests.
 */
@Entity
public class Employee {
    @Id
    private long id;
    private String name;
}
