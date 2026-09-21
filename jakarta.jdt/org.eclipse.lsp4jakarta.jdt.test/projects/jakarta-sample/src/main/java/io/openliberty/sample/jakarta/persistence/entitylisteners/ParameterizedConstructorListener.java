package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.PrePersist;

// Entity listener declaring only a parameterized constructor - invalid
public class ParameterizedConstructorListener {
    public ParameterizedConstructorListener(String name) {
    }

    @PrePersist
    public void beforePersist(Object entity) {
    }
}
