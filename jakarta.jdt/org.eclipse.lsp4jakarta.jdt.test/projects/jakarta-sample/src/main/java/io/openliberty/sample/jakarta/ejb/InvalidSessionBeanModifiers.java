package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.Stateful;
import jakarta.ejb.Singleton;

/**
 * Test resource file containing multiple top-level session bean classes that each
 * violate one or more constraints from Jakarta Enterprise Beans 4.0 spec section 4.1:
 * session bean classes must be public, non-final, non-abstract, and top-level.
 */

// Invalid: not declared public (package-private)
@Stateless
class NotPublicStatelessBean {
    public NotPublicStatelessBean() {
    }
}

// Invalid: declared final
@Stateless
public final class FinalStatelessBean {
    public FinalStatelessBean() {
    }
}

// Invalid: declared abstract
@Stateful
abstract class AbstractStatefulBean {
    public AbstractStatefulBean() {
    }
}

// Invalid: both not public and final
@Singleton
final class FinalNotPublicSingletonBean {
    public FinalNotPublicSingletonBean() {
    }
}
