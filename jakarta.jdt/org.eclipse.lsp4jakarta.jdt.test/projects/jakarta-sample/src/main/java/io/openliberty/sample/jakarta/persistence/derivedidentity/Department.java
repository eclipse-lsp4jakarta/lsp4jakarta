package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Parent entity whose @Id is a String — used to test type mismatch with ProjectId.empId (long).
 */
@Entity
public class Department {
    @Id
    private String deptCode;
    private String name;
}
