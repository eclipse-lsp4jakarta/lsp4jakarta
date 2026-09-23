package io.openliberty.sample.jakarta.persistence.bidirectional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Category entity with no back-reference to BidirectionalProduct,
 * confirming the relationship is unidirectional.
 * No diagnostic expected.
 */
@Entity
public class BidirectionalCategory {

    @Id
    private Long id;

    private String label;

    public BidirectionalCategory() {
    }
}
