package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

/**
 * Student entity in a bidirectional @ManyToMany relationship with BidirectionalCourse.
 * This is the inverse side — it has mappedBy referencing the owning-side field on Course.
 * No diagnostic is expected on this file.
 */
@Entity
public class BidirectionalStudent {

    @Id
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "students")
    private List<BidirectionalCourse> courses;

    public BidirectionalStudent() {
    }
}
