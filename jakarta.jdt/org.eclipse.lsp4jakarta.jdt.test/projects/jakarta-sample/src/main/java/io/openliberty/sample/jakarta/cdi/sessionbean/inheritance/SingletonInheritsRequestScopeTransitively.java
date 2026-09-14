package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Singleton;

// Test case 5: @Singleton inherits @RequestScoped transitively (grandparent -> intermediate -> child)
//              — should report error.
// 2 imports -> @Singleton on line 7, class decl on line 9
// "public class SingletonInheritsRequestScopeTransitively" -> col 13..53
@Singleton
public class SingletonInheritsRequestScopeTransitively extends ScopeInheritanceIntermediate {
}
