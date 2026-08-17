package io.openliberty.sample.jakarta.persistence.context;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.PersistenceContext;

// Valid: @PersistenceContext at the type level on a CDI-managed bean.
// @RequestScoped makes this a container-managed component.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@RequestScoped
@PersistenceContext(unitName = "myPU")
public class PersistenceContextOnTypeInCdiBeanValid {

    public void save(Object entity) {
    }
}
