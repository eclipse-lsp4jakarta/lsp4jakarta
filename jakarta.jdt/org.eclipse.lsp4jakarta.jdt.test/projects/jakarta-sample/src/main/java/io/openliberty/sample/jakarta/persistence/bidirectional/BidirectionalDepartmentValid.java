package io.openliberty.sample.jakarta.persistence.bidirectional;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

/**
 * Valid bidirectional @OneToMany inverse side — mappedBy is present and
 * @JoinTable is not used. No diagnostics are expected.
 */
@Entity
public class BidirectionalDepartmentValid {

    @Id
    private Long id;

    private String name;

    @OneToMany(mappedBy = "department")
    private List<BidirectionalEmployee> employees;

    public BidirectionalDepartmentValid() {
    }
}
