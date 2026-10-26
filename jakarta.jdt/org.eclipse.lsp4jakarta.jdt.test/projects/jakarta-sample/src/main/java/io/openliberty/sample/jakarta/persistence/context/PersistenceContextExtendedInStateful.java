package io.openliberty.sample.jakarta.persistence.context;

import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

// Valid: PersistenceContextType.EXTENDED is permitted only in a @Stateful EJB.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11810
@Stateful
public class PersistenceContextExtendedInStateful {

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    public void save(Object entity) {
        em.persist(entity);
    }
}
