package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Stateless;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;

// Invalid: Stateless with multiple scopes (Dependent + RequestScoped)
@Stateless
@Dependent
@RequestScoped
public class StatelessWithMultipleScopes {
}
