package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Stateless;
import jakarta.enterprise.context.Dependent;

// Valid: Stateless with only Dependent
@Stateless
@Dependent
public class StatelessWithDependent {
}
