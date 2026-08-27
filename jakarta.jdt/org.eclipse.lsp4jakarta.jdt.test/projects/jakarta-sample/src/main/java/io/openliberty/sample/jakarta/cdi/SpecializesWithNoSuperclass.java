package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: No superclass declared. @Specializes requires directly extending
 * a bean class, so this should trigger a diagnostic error.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithNoSuperclass {
    public String greet() { return "Custom Hello"; }
}
