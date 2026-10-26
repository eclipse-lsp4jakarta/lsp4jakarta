package io.openliberty.sample.jakarta.persistence.context;

import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Valid: @Singleton EJB with default (TRANSACTION) persistence context type.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@Singleton
public class PersistenceContextInSingletonValid {

    @PersistenceContext
    private EntityManager em;
}
