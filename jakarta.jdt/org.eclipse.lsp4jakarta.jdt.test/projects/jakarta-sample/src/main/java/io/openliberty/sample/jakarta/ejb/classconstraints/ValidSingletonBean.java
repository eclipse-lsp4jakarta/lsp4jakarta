package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Singleton;

// Valid: public, non-final, non-abstract, top-level — explicit no-arg constructor.
@Singleton
public class ValidSingletonBean {
    public ValidSingletonBean() {
    }
}
