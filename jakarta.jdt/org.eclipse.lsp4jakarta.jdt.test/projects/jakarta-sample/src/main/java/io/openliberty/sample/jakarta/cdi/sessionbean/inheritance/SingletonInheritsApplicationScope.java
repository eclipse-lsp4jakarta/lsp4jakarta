package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Singleton;

// Test case 2: @Singleton inherits @ApplicationScoped from direct superclass — valid, no diagnostic.
@Singleton
public class SingletonInheritsApplicationScope extends ScopeInheritanceParentWithApplicationScope {
}
