package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: Only implements an interface with no superclass. @Specializes requires
 * directly extending a bean class, so this should trigger a diagnostic error.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithInterfaceOnly implements BaseServiceInterface {
    @Override
    public String greet() { return "Custom Hello"; }
}
