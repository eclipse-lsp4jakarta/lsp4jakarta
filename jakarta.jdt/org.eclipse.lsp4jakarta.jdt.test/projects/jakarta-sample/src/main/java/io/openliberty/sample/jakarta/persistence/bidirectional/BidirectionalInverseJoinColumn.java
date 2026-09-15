package io.openliberty.sample.jakarta.persistence.bidirectional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

/**
 * Inverse-side entity that uses @JoinColumn on the inverse side — invalid.
 * The mappedBy attribute confirms this is the inverse side, but @JoinColumn
 * is also present which is only valid on the owning side.
 * This file is expected to produce a JoinColumnOnInverseSide diagnostic.
 */
@Entity
public class BidirectionalInverseJoinColumn {

    @Id
    private Long id;

    @OneToOne(mappedBy = "location")
    @JoinColumn(name = "location_id")
    private BidirectionalAddress address;

    public BidirectionalInverseJoinColumn() {
    }
}
