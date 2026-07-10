package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Stateful;

// Invalid: session bean class is declared abstract (also not public).
@Stateful
abstract class AbstractStatefulBean {
    public AbstractStatefulBean() {
    }
}
