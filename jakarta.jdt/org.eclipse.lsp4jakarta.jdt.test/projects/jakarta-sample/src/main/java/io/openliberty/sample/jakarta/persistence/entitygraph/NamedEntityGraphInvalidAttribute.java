package io.openliberty.sample.jakarta.persistence.entitygraph;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(
    name = "Author.withInvalidAttribute",
    attributeNodes = {
        @NamedAttributeNode("name"),
        @NamedAttributeNode("nonExistentField")
    }
)
public class NamedEntityGraphInvalidAttribute {

    @Id
    private Long id;

    private String name;

    public NamedEntityGraphInvalidAttribute() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
