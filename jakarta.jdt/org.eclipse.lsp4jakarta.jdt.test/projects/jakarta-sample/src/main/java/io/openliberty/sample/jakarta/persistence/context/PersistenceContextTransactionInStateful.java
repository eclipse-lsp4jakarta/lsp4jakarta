package io.openliberty.sample.jakarta.persistence.context;

import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Valid: @Stateful EJB with default (TRANSACTION) persistence context type.
// This should not trigger the EXTENDED diagnostic.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@Stateful
public class PersistenceContextTransactionInStateful {

    @PersistenceContext
    private EntityManager em;
}
