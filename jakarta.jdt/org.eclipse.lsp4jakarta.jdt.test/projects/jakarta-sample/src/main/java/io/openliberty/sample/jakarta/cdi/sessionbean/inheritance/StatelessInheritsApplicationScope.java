package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Stateless;

// Test case 9: @Stateless inherits @ApplicationScoped from direct superclass — should report error.
// 2 imports -> @Stateless on line 6, class decl on line 8
// "public class StatelessInheritsApplicationScope" -> col 13..45
@Stateless
public class StatelessInheritsApplicationScope extends ScopeInheritanceParentWithApplicationScope {
}
