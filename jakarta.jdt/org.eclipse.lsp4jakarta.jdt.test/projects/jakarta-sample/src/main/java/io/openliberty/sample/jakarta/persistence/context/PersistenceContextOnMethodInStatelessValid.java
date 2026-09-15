package io.openliberty.sample.jakarta.persistence.context;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Valid: @PersistenceContext at the method level on an EJB @Stateless session bean.
// Setter injection is a standard container-managed injection pattern.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@Stateless
public class PersistenceContextOnMethodInStatelessValid {

    private EntityManager em;

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
}
