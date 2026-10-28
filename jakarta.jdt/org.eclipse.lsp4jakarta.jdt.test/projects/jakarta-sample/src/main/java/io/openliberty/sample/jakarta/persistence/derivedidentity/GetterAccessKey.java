package io.openliberty.sample.jakarta.persistence.derivedidentity;

import java.io.Serializable;
import jakarta.persistence.Embeddable;

/**
 * Embeddable key with property-style access (getter methods, no matching fields).
 * getEmpId() returns long (matches Employee's PK).
 * isActive() returns boolean.
 */
@Embeddable
public class GetterAccessKey implements Serializable {
    private static final long serialVersionUID = 1L;

    public long getEmpId() {
        return 0L;
    }

    public boolean isActive() {
        return true;
    }
}
