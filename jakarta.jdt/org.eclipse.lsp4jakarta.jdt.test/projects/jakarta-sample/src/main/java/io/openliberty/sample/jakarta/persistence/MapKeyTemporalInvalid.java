package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.MapKeyTemporal;
import jakarta.persistence.TemporalType;
import java.util.Map;

@Entity
public class MapKeyTemporalInvalid {
    
    @Id
    private Long id;
    
    // Invalid: map key is String, not temporal
    @ElementCollection
    @MapKeyTemporal(TemporalType.DATE)
    private Map<String, String> stringEvents;
    
    // Invalid: map key is Integer, not temporal
    @ElementCollection
    @MapKeyTemporal(TemporalType.DATE)
    private Map<Integer, String> integerEvents;
    
    // Invalid: map key is Long, not temporal
    @ElementCollection
    @MapKeyTemporal(TemporalType.TIMESTAMP)
    private Map<Long, String> longEvents;
    
    // Invalid: getter with String map key
    @ElementCollection
    @MapKeyTemporal(TemporalType.DATE)
    public Map<String, String> getStringEvents() {
        return stringEvents;
    }
    
    public void setStringEvents(Map<String, String> stringEvents) {
        this.stringEvents = stringEvents;
    }
    
    // Invalid: FQN String map key (should still be detected as invalid)
    @ElementCollection
    @MapKeyTemporal(TemporalType.DATE)
    private Map<java.lang.String, String> fqnStringEvents;
}
