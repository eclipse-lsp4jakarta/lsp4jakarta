package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

/**
 * Course entity — the inverse side of a bidirectional @ManyToMany with
 * BidirectionalStudent. mappedBy is MISSING.
 * Expected to produce an InverseSideMissingMappedBy diagnostic.
 */
@Entity
public class BidirectionalCourse {

    @Id
    private Long id;

    private String title;

    @ManyToMany
    private List<BidirectionalStudent> students;

    public BidirectionalCourse() {
    }
}
