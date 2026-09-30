package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

// Intermediate class with no scope — used to test transitive inheritance.
// Its effective scope is @RequestScoped inherited from ScopeInheritanceParentWithRequestScope.
public class ScopeInheritanceIntermediate extends ScopeInheritanceParentWithRequestScope {
}
