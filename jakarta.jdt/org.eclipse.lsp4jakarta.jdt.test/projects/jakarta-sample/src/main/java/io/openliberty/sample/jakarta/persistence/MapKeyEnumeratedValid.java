package io.openliberty.sample.jakarta.persistence;

import java.util.Map;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyEnumerated;

@Entity
public class MapKeyEnumeratedValid {

    @Id
    private Long id;

    // Valid: map key is an enum — no diagnostic expected
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    private Map<MapKeyRole, String> roleDescriptions;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }
}
