package io.openliberty.sample.jakarta.cdi;

import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;

// ---------------------------------------------------------------------------
// Helper parent classes used by the inheritance test cases below.
// CDI scope annotations are @Inherited, so they propagate through the
// superclass chain only — not through implemented interfaces.
// ---------------------------------------------------------------------------

// Parent with an invalid scope for @Singleton (RequestScoped)
@RequestScoped
class ScopeInheritanceParentWithRequestScope {
}

// Parent with a valid scope for @Singleton (ApplicationScoped)
@ApplicationScoped
class ScopeInheritanceParentWithApplicationScope {
}

// Parent with a valid scope for @Singleton and @Stateless (Dependent)
@Dependent
class ScopeInheritanceParentWithDependentScope {
}

// Parent with an invalid scope (SessionScoped)
@SessionScoped
class ScopeInheritanceParentWithSessionScope {
}

// Intermediate class with no scope — used to test transitive inheritance.
// Its effective scope is @RequestScoped inherited from its own parent.
class ScopeInheritanceIntermediate extends ScopeInheritanceParentWithRequestScope {
}

// ---------------------------------------------------------------------------
// @Singleton inheritance test cases
// ---------------------------------------------------------------------------

// Test case 1: @Singleton inherits @RequestScoped from superclass — should report error (class on line 47)
@Singleton
class SingletonInheritsRequestScope extends ScopeInheritanceParentWithRequestScope {
}

// Test case 2: @Singleton inherits @ApplicationScoped from superclass — valid, no diagnostic
@Singleton
class SingletonInheritsApplicationScope extends ScopeInheritanceParentWithApplicationScope {
}

// Test case 3: @Singleton inherits @Dependent from superclass — valid, no diagnostic
@Singleton
class SingletonInheritsDependentScope extends ScopeInheritanceParentWithDependentScope {
}

// Test case 4: @Singleton inherits @SessionScoped from superclass — should report error (class on line 62)
@Singleton
class SingletonInheritsSessionScope extends ScopeInheritanceParentWithSessionScope {
}

// Test case 5: @Singleton inherits @RequestScoped transitively (grandparent -> intermediate -> child)
//              — should report error (class on line 68)
@Singleton
class SingletonInheritsRequestScopeTransitively extends ScopeInheritanceIntermediate {
}

// Test case 6: @Singleton has its own @ApplicationScoped — own scope overrides parent @RequestScoped
//              — valid, no diagnostic
@ApplicationScoped
@Singleton
class SingletonWithOwnScopeOverridesInheritedScope extends ScopeInheritanceParentWithRequestScope {
}

// ---------------------------------------------------------------------------
// @Stateless inheritance test cases
// ---------------------------------------------------------------------------

// Test case 7: @Stateless inherits @RequestScoped from superclass — should report error (class on line 84)
@Stateless
class StatelessInheritsRequestScope extends ScopeInheritanceParentWithRequestScope {
}

// Test case 8: @Stateless inherits @Dependent from superclass — valid, no diagnostic
@Stateless
class StatelessInheritsDependentScope extends ScopeInheritanceParentWithDependentScope {
}

// Test case 9: @Stateless inherits @ApplicationScoped from superclass — should report error (class on line 94)
@Stateless
class StatelessInheritsApplicationScope extends ScopeInheritanceParentWithApplicationScope {
}

// Test case 10: @Stateless inherits @RequestScoped transitively — should report error (class on line 99)
@Stateless
class StatelessInheritsRequestScopeTransitively extends ScopeInheritanceIntermediate {
}

// Test case 11: @Stateless has its own @Dependent — own scope overrides parent @RequestScoped
//               — valid, no diagnostic
@Dependent
@Stateless
class StatelessWithOwnScopeOverridesInheritedScope extends ScopeInheritanceParentWithRequestScope {
}
