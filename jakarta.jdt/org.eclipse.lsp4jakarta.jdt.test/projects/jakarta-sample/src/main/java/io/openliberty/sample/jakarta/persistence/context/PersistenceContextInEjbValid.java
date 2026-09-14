package io.openliberty.sample.jakarta.persistence.context;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Valid: EJB stateless session bean annotated with @Stateless.
// @PersistenceContext injection is valid in EJB session beans.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@Stateless
public class PersistenceContextInEjbValid {

    @PersistenceContext
    private EntityManager em;

    public Object find(Class<?> entityClass, Long id) {
        return em.find(entityClass, id);
    }
}
