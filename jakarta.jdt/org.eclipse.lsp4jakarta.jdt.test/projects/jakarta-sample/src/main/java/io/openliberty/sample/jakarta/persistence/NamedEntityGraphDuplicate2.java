package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(name = "User.graph")
public class NamedEntityGraphDuplicate2 {

    @Id
    private Long id;

    private String role;

    public NamedEntityGraphDuplicate2() {
    }
}
