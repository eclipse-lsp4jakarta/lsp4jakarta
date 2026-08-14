package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Stateless;

// Valid: public, non-final, non-abstract, top-level — explicit no-arg constructor.
@Stateless
public class ValidStatelessBean {
    public ValidStatelessBean() {
    }
}
