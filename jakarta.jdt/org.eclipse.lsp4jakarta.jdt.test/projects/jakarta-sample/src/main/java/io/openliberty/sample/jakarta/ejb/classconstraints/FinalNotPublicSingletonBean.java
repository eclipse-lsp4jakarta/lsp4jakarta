package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Singleton;

// Invalid: session bean class is final and not public.
@Singleton
final class FinalNotPublicSingletonBean {
    public FinalNotPublicSingletonBean() {
    }
}
