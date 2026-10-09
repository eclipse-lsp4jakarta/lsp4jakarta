package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.enterprise.context.ApplicationScoped;

// Helper parent: declares @ApplicationScoped (valid for @Singleton, invalid for @Stateless).
@ApplicationScoped
public class ScopeInheritanceParentWithApplicationScope {
}
