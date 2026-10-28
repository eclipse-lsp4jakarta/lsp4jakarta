package io.openliberty.sample.jakarta.persistence.bidirectional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToOne;

/**
 * Inverse-side entity that uses @JoinColumns on the inverse side — invalid.
 * The mappedBy attribute confirms this is the inverse side, but @JoinColumns
 * is also present which is only valid on the owning side.
 * This file is expected to produce a JoinColumnOnInverseSide diagnostic.
 */
@Entity
public class BidirectionalInverseJoinColumns {

    @Id
    private Long id;

    @OneToOne(mappedBy = "location")
    @JoinColumns({
        @JoinColumn(name = "loc_id"),
        @JoinColumn(name = "loc_type")
    })
    private BidirectionalAddress address;

    public BidirectionalInverseJoinColumns() {
    }
}
