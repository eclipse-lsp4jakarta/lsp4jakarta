package io.openliberty.sample.jakarta.persistence.bidirectional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * Valid inverse side of a bidirectional @OneToOne — mappedBy is present.
 * No diagnostic expected.
 */
@Entity
public class BidirectionalPersonValid {

    @Id
    private Long id;

    private String name;

    @OneToOne(mappedBy = "resident")
    private BidirectionalAddress address;

    public BidirectionalPersonValid() {
    }
}
