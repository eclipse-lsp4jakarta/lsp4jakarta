package io.openliberty.sample.jakarta.persistence.context;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Invalid: two @PersistenceContext fields in a plain (unmanaged) class.
// Each annotated field should independently trigger the diagnostic.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
public class PersistenceContextMultipleFieldsInPlainClass {

    @PersistenceContext
    private EntityManager emA;

    @PersistenceContext
    private EntityManager emB;
}
