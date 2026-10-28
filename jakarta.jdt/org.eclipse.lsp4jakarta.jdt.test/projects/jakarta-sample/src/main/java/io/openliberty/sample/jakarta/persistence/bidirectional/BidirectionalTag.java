package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

/**
 * Owning side of a bidirectional @ManyToMany with BidirectionalPost.
 * No mappedBy — this is the owning side, so no diagnostic expected.
 */
@Entity
public class BidirectionalTag {

    @Id
    private Long id;

    private String label;

    @ManyToMany
    private List<BidirectionalPost> posts;

    public BidirectionalTag() {
    }
}
