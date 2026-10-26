package io.openliberty.sample.jakarta.persistence.context;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Valid: CDI managed bean annotated with @ApplicationScoped.
// @PersistenceContext injection is valid in CDI beans.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@ApplicationScoped
public class PersistenceContextInCdiBeanValid {

    @PersistenceContext
    private EntityManager em;

    public void save(Object entity) {
        em.persist(entity);
    }
}
