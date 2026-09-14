package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Stateless;

// Valid: Stateless with no explicit scope (defaults to @Dependent)
@Stateless
public class StatelessWithNoScope {
}
