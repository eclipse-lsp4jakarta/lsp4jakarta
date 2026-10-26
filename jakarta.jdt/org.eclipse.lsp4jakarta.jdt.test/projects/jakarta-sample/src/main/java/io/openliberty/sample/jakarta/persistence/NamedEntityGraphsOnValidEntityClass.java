package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;

@Entity
@NamedEntityGraphs({ @NamedEntityGraph(name = "Graph.User"), @NamedEntityGraph(name = "Graph.Order") })
public class NamedEntityGraphsOnValidEntityClass {
    @Id
    private Long id;
}
