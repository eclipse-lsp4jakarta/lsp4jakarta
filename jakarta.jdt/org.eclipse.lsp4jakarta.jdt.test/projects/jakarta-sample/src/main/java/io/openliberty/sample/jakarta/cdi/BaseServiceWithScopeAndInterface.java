package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * A CDI bean with @ApplicationScoped scope annotation.
 * Used as the unique base for SpecializesWithBeanSuperclassAndInterface.
 */
@ApplicationScoped
public class BaseServiceWithScopeAndInterface {
    public String greet() { return "Hello"; }
}
