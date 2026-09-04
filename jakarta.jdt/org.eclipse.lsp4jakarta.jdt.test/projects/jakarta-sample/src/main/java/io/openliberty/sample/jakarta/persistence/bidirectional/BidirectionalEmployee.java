package io.openliberty.sample.jakarta.persistence.bidirectional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Owning-side entity used by bidirectional relationship tests.
 * The @ManyToOne back-reference to BidirectionalDepartment makes the
 * BidirectionalDepartment.employees field the inverse side of the relationship.
 */
@Entity
public class BidirectionalEmployee {

    @Id
    private Long id;

    private String name;

    @ManyToOne
    private BidirectionalDepartment department;

    public BidirectionalEmployee() {
    }
}
