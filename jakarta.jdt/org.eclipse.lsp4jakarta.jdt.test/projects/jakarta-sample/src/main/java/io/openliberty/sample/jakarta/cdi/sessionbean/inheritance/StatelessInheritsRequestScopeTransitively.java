package io.openliberty.sample.jakarta.cdi.sessionbean.inheritance;

import jakarta.ejb.Stateless;

// Test case 10: @Stateless inherits @RequestScoped transitively (grandparent -> intermediate -> child)
//               — should report error.
// 2 imports -> @Stateless on line 7, class decl on line 9
// "public class StatelessInheritsRequestScopeTransitively" -> col 13..55
@Stateless
public class StatelessInheritsRequestScopeTransitively extends ScopeInheritanceIntermediate {
}
