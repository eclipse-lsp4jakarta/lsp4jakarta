package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Singleton;

// Test case 3: @Singleton inherits @Dependent from direct superclass — valid, no diagnostic.
@Singleton
public class SingletonInheritsDependentScope extends ScopeInheritanceParentWithDependentScope {
}
