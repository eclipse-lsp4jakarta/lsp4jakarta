package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * A CDI bean with @ApplicationScoped scope annotation.
 */
@ApplicationScoped
public class BaseServiceWithScope {
    public String greet() { return "Hello"; }
}
