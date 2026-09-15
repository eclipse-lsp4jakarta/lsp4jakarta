package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.Dependent;

/**
 * A CDI bean with @Dependent scope annotation.
 */
@Dependent
public class BaseServiceWithDependentScope {
    public String greet() { return "Hello"; }
}
