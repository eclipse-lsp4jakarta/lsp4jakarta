package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(name = "Admin.uniqueGraph")
public class NamedEntityGraphUnique {

    @Id
    private Long id;

    private String username;

    public NamedEntityGraphUnique() {
    }
}
