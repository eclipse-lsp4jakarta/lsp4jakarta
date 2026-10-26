package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Stateless;
import jakarta.enterprise.context.Dependent;

// Test case 11: @Stateless has its own @Dependent — own scope overrides inherited @RequestScoped
//               — valid, no diagnostic.
@Dependent
@Stateless
public class StatelessWithOwnScopeOverridesInheritedScope extends ScopeInheritanceParentWithRequestScope {
}
