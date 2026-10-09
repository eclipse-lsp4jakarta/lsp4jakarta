package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.ApplicationScoped;

// Test case 6: @Singleton has its own @ApplicationScoped — own scope overrides inherited @RequestScoped
//              — valid, no diagnostic.
@ApplicationScoped
@Singleton
public class SingletonWithOwnScopeOverridesInheritedScope extends ScopeInheritanceParentWithRequestScope {
}
