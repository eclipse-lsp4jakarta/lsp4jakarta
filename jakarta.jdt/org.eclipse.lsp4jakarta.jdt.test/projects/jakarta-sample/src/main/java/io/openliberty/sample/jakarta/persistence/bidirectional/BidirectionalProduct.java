package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

/**
 * Product entity with a unidirectional @ManyToMany relationship.
 * BidirectionalCategory has no back-reference to Product,
 * so no diagnostic is expected on either side.
 */
@Entity
public class BidirectionalProduct {

    @Id
    private Long id;

    private String name;

    @ManyToMany
    private List<BidirectionalCategory> categories;

    public BidirectionalProduct() {
    }
}
