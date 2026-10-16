package io.openliberty.sample.jakarta.persistence.entitygraph;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(
    name = "SubclassEntity.invalid",
    attributeNodes = {
        @NamedAttributeNode("invalidSuperField"),
        @NamedAttributeNode("invalidMethodProperty")
    }
)
public class NamedEntityGraphSubclassInvalid extends NamedEntityGraphSuperclass {

    @Id
    private Long id;

    public NamedEntityGraphSubclassInvalid() {
    }

    public Long getId() {
        return id;
    }

    public void setParamOnly(String value) {
    }

    public void getVoidMethod() {
    }
}
