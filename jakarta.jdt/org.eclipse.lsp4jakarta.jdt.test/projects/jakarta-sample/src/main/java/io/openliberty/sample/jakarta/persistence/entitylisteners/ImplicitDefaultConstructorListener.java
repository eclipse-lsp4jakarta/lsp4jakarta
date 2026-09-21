package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.PrePersist;

// Entity listener with implicit default constructor - valid
public class ImplicitDefaultConstructorListener {
    @PrePersist
    public void beforePersist(Object entity) {
    }
}
