package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;

@Entity
@NamedEntityGraphs({
        @NamedEntityGraph(name = "Container.graphA"),
        @NamedEntityGraph(name = "Container.graphB")
})
public class NamedEntityGraphsContainerUnique {

    @Id
    private Long id;

    private String name;

    public NamedEntityGraphsContainerUnique() {
    }
}
