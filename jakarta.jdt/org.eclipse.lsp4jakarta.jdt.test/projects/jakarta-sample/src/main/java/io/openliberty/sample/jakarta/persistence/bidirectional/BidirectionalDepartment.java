package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

/**
 * Inverse-side entity that is MISSING the mappedBy attribute.
 * BidirectionalEmployee has a @ManyToOne back-reference to this class,
 * so this is the inverse side and MUST declare mappedBy.
 * This file is expected to produce an InverseSideMissingMappedBy diagnostic.
 */
@Entity
public class BidirectionalDepartment {

    @Id
    private Long id;

    private String name;

    @OneToMany
    private List<BidirectionalEmployee> employees;

    public BidirectionalDepartment() {
    }
}
