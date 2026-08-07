package io.openliberty.sample.jakarta.persistence;

import java.util.Map;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyEnumerated;

@Entity
public class MapKeyEnumeratedNonEnumKey {

    @Id
    private Long id;

    // Invalid: map key is String, not an enum
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    private Map<String, String> roleDescriptions;

    // Invalid: map key is Integer, not an enum
    @ElementCollection
    @MapKeyEnumerated(EnumType.ORDINAL)
    private Map<Integer, String> priorityDescriptions;
    
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    private Map roleDescriptions2;
    
    // Invalid: wildcard key — cannot guarantee it is an enum
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    private Map<?, String> wildcardKeyMap;
    
    // Invalid: method returns Map with non-enum key
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    public Map<String, String> getStringKeyMap() {
        return this.roleDescriptions;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }
}
