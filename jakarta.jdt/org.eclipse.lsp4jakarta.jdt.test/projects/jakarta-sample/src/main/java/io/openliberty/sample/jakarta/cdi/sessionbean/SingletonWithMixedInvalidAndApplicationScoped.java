package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;

// Test case 6: Singleton with mixed invalid+valid scopes (RequestScoped + ApplicationScoped) - should report error
@Singleton
@RequestScoped
@ApplicationScoped
public class SingletonWithMixedInvalidAndApplicationScoped {
}
