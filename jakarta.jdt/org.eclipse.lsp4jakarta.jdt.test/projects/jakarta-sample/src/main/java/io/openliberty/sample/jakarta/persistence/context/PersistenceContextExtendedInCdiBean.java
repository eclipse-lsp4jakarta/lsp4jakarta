package io.openliberty.sample.jakarta.persistence.context;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

// Invalid: PersistenceContextType.EXTENDED in a CDI @ApplicationScoped bean.
// EXTENDED is an EJB-only concept; only @Stateful session beans may use it (§7.6.3).
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11810
@ApplicationScoped
public class PersistenceContextExtendedInCdiBean {

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager em;
}
