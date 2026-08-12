package io.openliberty.sample.jakarta.persistence.context;

import jakarta.persistence.PersistenceContext;

// Plain Java class with no CDI, EJB, or Servlet annotation.
// @PersistenceContext at the type level requires a container-managed component.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@PersistenceContext(unitName = "myPU")
public class PersistenceContextOnTypeInPlainClass {

    public void save(Object entity) {
    }
}
