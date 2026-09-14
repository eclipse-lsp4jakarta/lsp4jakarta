package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.ApplicationScoped;

// Test case 3: Singleton with valid scope (ApplicationScoped) - should NOT report error
@Singleton
@ApplicationScoped
public class SingletonWithApplicationScope {
}
