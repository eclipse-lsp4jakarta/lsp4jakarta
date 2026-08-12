package io.openliberty.sample.jakarta.persistence.context;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Plain Java class with no CDI, EJB, or Servlet annotation.
// @PersistenceContext at the method level requires a container-managed component.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
public class PersistenceContextOnMethodInPlainClass {

    private EntityManager em;

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
}
