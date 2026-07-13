package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.MapKeyTemporal;
import jakarta.persistence.TemporalType;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;

@Entity
public class MapKeyTemporalValid {
    
    @Id
    private Long id;
    
    // Valid: map key is Date
    @ElementCollection
    @MapKeyTemporal(TemporalType.DATE)
    private Map<Date, String> dateEvents;
    
    // Valid: map key is Calendar
    @ElementCollection
    @MapKeyTemporal(TemporalType.TIMESTAMP)
    private Map<Calendar, String> calendarEvents;
    
    // Valid: getter with Date map key
    @ElementCollection
    @MapKeyTemporal(TemporalType.DATE)
    public Map<Date, String> getDateEvents() {
        return dateEvents;
    }
    
    public void setDateEvents(Map<Date, String> dateEvents) {
        this.dateEvents = dateEvents;
    }
    
    // Valid: FQN Date map key
    @ElementCollection
    @MapKeyTemporal(TemporalType.DATE)
    private Map<java.util.Date, String> fqnDateEvents;
    
    // Valid: FQN Calendar map key
    @ElementCollection
    @MapKeyTemporal(TemporalType.TIMESTAMP)
    private Map<java.util.Calendar, String> fqnCalendarEvents;
}

