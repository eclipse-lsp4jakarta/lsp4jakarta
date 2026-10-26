package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Stateless;

// Invalid: session bean class is not declared public (package-private).
@Stateless
class NotPublicStatelessBean {
    public NotPublicStatelessBean() {
    }
}
