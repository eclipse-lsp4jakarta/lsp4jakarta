package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

/**
 * Inverse side of a bidirectional @ManyToMany — has mappedBy but ALSO
 * has @JoinTable, which is invalid on the inverse side.
 * Expected to produce a JoinTableOnInverseSide diagnostic.
 */
@Entity
public class BidirectionalPost {

    @Id
    private Long id;

    private String title;

    @ManyToMany(mappedBy = "posts")
    @JoinTable(name = "POST_TAG")
    private List<BidirectionalTag> tags;

    public BidirectionalPost() {
    }
}
