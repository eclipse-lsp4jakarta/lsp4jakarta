package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Stateless;

// Invalid: session bean class is declared final.
@Stateless
public final class FinalStatelessBean {
    public FinalStatelessBean() {
    }
}
