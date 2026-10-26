package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Singleton;

// Test case 1: @Singleton inherits @RequestScoped from direct superclass — should report error.
// 2 imports -> @Singleton on line 6, class decl on line 8
// "public class SingletonInheritsRequestScope" -> col 13..41
@Singleton
public class SingletonInheritsRequestScope extends ScopeInheritanceParentWithRequestScope {
}
