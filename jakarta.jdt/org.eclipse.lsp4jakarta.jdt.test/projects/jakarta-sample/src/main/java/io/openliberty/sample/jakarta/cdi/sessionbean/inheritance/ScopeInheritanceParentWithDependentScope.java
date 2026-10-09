package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.enterprise.context.Dependent;

// Helper parent: declares @Dependent (valid for both @Singleton and @Stateless).
@Dependent
public class ScopeInheritanceParentWithDependentScope {
}
