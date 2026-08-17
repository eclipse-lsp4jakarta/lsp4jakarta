package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Singleton;

// Invalid: session bean class is declared final.
@Singleton
public final class FinalNotPublicSingletonBean {
    public FinalNotPublicSingletonBean() {
    }
}
