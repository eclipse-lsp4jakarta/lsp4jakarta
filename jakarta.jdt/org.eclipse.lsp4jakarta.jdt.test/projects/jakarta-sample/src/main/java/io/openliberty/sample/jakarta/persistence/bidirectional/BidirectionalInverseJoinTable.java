package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;

/**
 * Inverse-side entity that uses @JoinTable on the inverse side — invalid.
 * The mappedBy attribute is set, confirming this is the inverse side,
 * but @JoinTable is also present which is not allowed on the inverse side.
 * This file is expected to produce a JoinTableOnInverseSide diagnostic.
 */
@Entity
public class BidirectionalInverseJoinTable {

    @Id
    private Long id;

    private String name;

    @OneToMany(mappedBy = "department")
    @JoinTable(name = "DEPT_EMP")
    private List<BidirectionalEmployee> employees;

    public BidirectionalInverseJoinTable() {
    }
}
