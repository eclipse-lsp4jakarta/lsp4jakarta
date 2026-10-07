package io.openliberty.sample.jakarta.persistence.bidirectional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * Owning side of a bidirectional @OneToOne — mappedBy is MISSING.
 * BidirectionalAddress has a @OneToOne(mappedBy="address") back-reference to this class,
 * confirming this is the owning side without mappedBy declared.
 * Expected to produce an InverseSideMissingMappedBy diagnostic.
 */
@Entity
public class BidirectionalPerson {

    @Id
    private Long id;

    private String name;

    @OneToOne
    private BidirectionalAddress address;

    public BidirectionalPerson() {
    }
}
