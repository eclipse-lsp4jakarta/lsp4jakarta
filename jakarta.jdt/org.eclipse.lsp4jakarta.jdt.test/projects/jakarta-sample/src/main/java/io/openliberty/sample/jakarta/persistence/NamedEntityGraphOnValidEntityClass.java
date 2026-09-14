package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(name = "Graph.User")
public class NamedEntityGraphOnValidEntityClass {
    @Id
    private Long id;
}
