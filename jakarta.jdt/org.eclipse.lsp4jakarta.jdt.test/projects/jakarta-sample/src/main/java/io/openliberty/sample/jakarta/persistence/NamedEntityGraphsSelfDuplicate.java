package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;

@Entity
@NamedEntityGraphs({
        @NamedEntityGraph(name = "Self.duplicate"),
        @NamedEntityGraph(name = "Self.duplicate")
})
public class NamedEntityGraphsSelfDuplicate {

    @Id
    private Long id;

    private String name;

    public NamedEntityGraphsSelfDuplicate() {
    }
}
