package io.openliberty.sample.jakarta.persistence;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyEnumerated;

@Entity
public class MapKeyEnumeratedNotOnMap {

    @Id
    private Long id;

    // Invalid: @MapKeyEnumerated on a List — no concept of a map key
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    private List<String> roles;

    // Invalid: @MapKeyEnumerated on a plain String field
    @MapKeyEnumerated(EnumType.STRING)
    private String name;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }
}
