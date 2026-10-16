package io.openliberty.sample.jakarta.persistence.entitygraph;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(
    name = "SubclassEntity.valid",
    attributeNodes = {
        @NamedAttributeNode("id"),
        @NamedAttributeNode("subclassField"),
        @NamedAttributeNode("methodProperty"),
        @NamedAttributeNode("active"),
        @NamedAttributeNode("superField"),
        @NamedAttributeNode("superGetterProperty"),
        @NamedAttributeNode("superActive")
    }
)
public class NamedEntityGraphSubclassValid extends NamedEntityGraphSuperclass {

    @Id
    private Long id;

    private String subclassField;

    public NamedEntityGraphSubclassValid() {
    }

    public Long getId() {
        return id;
    }

    public String getSubclassField() {
        return subclassField;
    }

    public String getMethodProperty() {
        return "methodProperty";
    }

    public boolean isActive() {
        return true;
    }
}
