package io.openliberty.sample.jakarta.persistence.bidirectional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Item entity for unidirectional order test — has NO back-reference to
 * BidirectionalOrder, confirming the relationship is unidirectional.
 */
@Entity
public class BidirectionalOrderItem {

    @Id
    private Long id;

    private String description;

    public BidirectionalOrderItem() {
    }
}
