package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;

@Entity
@NamedEntityGraphs({
        @NamedEntityGraph(name = "User.graph"),
        @NamedEntityGraph(name = "Container.uniqueGraph")
})
public class NamedEntityGraphsContainerDuplicate {

    @Id
    private Long id;

    private String name;

    public NamedEntityGraphsContainerDuplicate() {
    }
}
