package io.openliberty.sample.jakarta.persistence.entitygraph;

import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(
    name = "Order.validGraph",
    attributeNodes = {
        @NamedAttributeNode("items"),
        @NamedAttributeNode("total")
    }
)
public class NamedEntityGraphValid {

    @Id
    private Long id;

    private List<String> items;

    private Double total;

    public NamedEntityGraphValid() {
    }

    public Long getId() {
        return id;
    }

    public List<String> getItems() {
        return items;
    }

    public Double getTotal() {
        return total;
    }
}
