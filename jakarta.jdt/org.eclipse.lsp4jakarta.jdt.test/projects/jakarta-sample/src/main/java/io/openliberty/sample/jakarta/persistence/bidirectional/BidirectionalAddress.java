package io.openliberty.sample.jakarta.persistence.bidirectional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * Inverse side of a bidirectional @OneToOne — explicitly declares mappedBy,
 * making BidirectionalPerson.address the owning-side field.
 * No diagnostic expected here (mappedBy is correctly set).
 */
@Entity
public class BidirectionalAddress {

    @Id
    private Long id;

    private String street;

    @OneToOne(mappedBy = "address")
    private BidirectionalPerson resident;

    public BidirectionalAddress() {
    }
}
