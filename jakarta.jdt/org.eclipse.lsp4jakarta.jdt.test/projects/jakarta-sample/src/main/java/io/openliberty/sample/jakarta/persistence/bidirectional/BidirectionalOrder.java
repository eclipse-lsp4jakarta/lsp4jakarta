package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

/**
 * Unidirectional @OneToMany relationship — no back-reference exists in BidirectionalOrderItem.
 * This is perfectly valid and must NOT produce any diagnostic.
 */
@Entity
public class BidirectionalOrder {

    @Id
    private Long id;

    @OneToMany
    private List<BidirectionalOrderItem> items;

    public BidirectionalOrder() {
    }
}
