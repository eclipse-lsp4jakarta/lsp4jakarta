package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Stateful;

// Invalid: session bean class is declared abstract.
@Stateful
public abstract class AbstractStatefulBean {
    public AbstractStatefulBean() {
    }
}
