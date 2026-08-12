package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;

// Test case 8: Singleton with both valid scopes (ApplicationScoped + Dependent) - should NOT report error
@Singleton
@ApplicationScoped
@Dependent
public class SingletonWithBothValidScopes {
}
