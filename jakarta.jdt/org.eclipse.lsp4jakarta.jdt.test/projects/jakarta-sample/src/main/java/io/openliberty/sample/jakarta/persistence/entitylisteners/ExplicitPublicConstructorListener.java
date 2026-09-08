package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.PrePersist;

// Entity listener declaring an explicit public no-arg constructor - valid
public class ExplicitPublicConstructorListener {
    public ExplicitPublicConstructorListener() {
    }

    @PrePersist
    public void beforePersist(Object entity) {
    }
}
