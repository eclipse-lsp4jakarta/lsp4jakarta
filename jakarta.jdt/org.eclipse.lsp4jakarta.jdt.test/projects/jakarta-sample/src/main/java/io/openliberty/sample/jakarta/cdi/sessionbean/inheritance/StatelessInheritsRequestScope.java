package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Stateless;

// Test case 7: @Stateless inherits @RequestScoped from direct superclass — should report error.
// 2 imports -> @Stateless on line 6, class decl on line 8
// "public class StatelessInheritsRequestScope" -> col 13..41
@Stateless
public class StatelessInheritsRequestScope extends ScopeInheritanceParentWithRequestScope {
}
