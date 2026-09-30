package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.enterprise.context.SessionScoped;

// Helper parent: declares @SessionScoped (invalid for @Singleton and @Stateless).
@SessionScoped
public class ScopeInheritanceParentWithSessionScope {
}
