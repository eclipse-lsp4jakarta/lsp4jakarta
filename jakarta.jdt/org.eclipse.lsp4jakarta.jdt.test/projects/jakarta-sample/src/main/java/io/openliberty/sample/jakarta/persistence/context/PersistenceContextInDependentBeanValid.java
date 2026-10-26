package io.openliberty.sample.jakarta.persistence.context;

import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Valid: CDI managed bean with @Dependent scope.
// All CDI scoped beans are container-managed; @PersistenceContext injection is valid.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@Dependent
public class PersistenceContextInDependentBeanValid {

    @PersistenceContext
    private EntityManager em;

    public void save(Object entity) {
        em.persist(entity);
    }
}
