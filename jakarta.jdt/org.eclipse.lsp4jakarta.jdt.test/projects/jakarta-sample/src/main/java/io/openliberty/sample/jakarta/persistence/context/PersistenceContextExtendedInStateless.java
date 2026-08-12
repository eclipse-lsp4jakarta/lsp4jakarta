package io.openliberty.sample.jakarta.persistence.context;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

// Invalid: PersistenceContextType.EXTENDED can only be used in a @Stateful EJB.
// @Stateless beans are transaction-scoped; EXTENDED context only survives the lifecycle
// of a stateful bean, not a stateless one.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11810
@Stateless
public class PersistenceContextExtendedInStateless {

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    public Object find(Class<?> entityClass, Long id) {
        return em.find(entityClass, id);
    }
}
