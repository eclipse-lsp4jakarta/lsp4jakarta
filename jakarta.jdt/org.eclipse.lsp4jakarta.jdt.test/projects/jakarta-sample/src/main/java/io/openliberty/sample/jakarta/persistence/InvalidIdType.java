package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test class with invalid @Id types that should trigger diagnostics
 */
@Entity
public class InvalidIdType {

    // Invalid: Custom class type
    @Id
    private CustomType customId;

    // Invalid: UUID type (not in specification)
    @Id
    private UUID uuidId;

    // Invalid: Collection type
    @Id
    private List<String> listId;
    
    @Id
    private Set<String> setId;
    
    @Id
    private Map<String, String> mapId;

    // Invalid: Object type
    @Id
    private Object objectId;

    // Invalid: Array type
    @Id
    private int[] arrayId;

    // Valid types for comparison (should not trigger diagnostics)
    @Id
    private long validPrimitive;

    @Id
    private String validString;

    // Getter with invalid return type
    @Id
    public CustomType getCustomId() {
        return customId;
    }

    // Getter with invalid UUID return type
    @Id
    public UUID getUuidId() {
        return uuidId;
    }

    // Getter with valid return type
    @Id
    public Long getValidWrapperId() {
        return 1L;
    }
}

// Custom type class for testing
class CustomType {
    private String value;
}

