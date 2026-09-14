package io.openliberty.sample.jakarta.persistence.context;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;

// Invalid: @Entity marks a JPA entity; it is NOT a container-managed injection component.
// @PersistenceContext injection is not valid here.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@Entity
public class PersistenceContextInEntityClass {

    @Id
    private Long id;

    @PersistenceContext
    private EntityManager em;
}
