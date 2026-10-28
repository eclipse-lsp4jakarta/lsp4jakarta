package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.PrePersist;

// Package-private class with no declared constructor.
// The compiler synthesises a package-private default constructor (JLS 8.8.9) — not public.
class PackagePrivateImplicitConstructorListener {

    @PrePersist
    public void beforePersist(Object entity) {
    }
}
