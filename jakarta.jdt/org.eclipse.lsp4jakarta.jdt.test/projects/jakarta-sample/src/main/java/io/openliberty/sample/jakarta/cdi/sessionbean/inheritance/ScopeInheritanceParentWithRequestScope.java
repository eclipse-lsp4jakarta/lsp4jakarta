package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.enterprise.context.RequestScoped;

// Helper parent: declares @RequestScoped (invalid for @Singleton and @Stateless).
@RequestScoped
public class ScopeInheritanceParentWithRequestScope {
}
