package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.PrePersist;

// Entity listener declaring only a protected no-arg constructor - invalid
public class ProtectedConstructorListener {
    protected ProtectedConstructorListener() {
    }

    @PrePersist
    public void beforePersist(Object entity) {
    }
}
