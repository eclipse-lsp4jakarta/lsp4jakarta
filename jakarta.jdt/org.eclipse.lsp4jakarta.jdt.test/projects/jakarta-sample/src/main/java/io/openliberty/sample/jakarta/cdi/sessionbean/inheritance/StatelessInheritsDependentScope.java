package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Stateless;

// Test case 8: @Stateless inherits @Dependent from direct superclass — valid, no diagnostic.
@Stateless
public class StatelessInheritsDependentScope extends ScopeInheritanceParentWithDependentScope {
}
