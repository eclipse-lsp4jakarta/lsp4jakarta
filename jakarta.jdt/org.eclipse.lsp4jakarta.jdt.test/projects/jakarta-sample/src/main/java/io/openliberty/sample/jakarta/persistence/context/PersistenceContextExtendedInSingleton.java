package io.openliberty.sample.jakarta.persistence.context;

import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

// Invalid: PersistenceContextType.EXTENDED in a @Singleton EJB.
// EXTENDED is only permitted in @Stateful session beans (§7.6.3).
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11810
@Singleton
public class PersistenceContextExtendedInSingleton {

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager em;
}
