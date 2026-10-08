package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: MiddleServiceNoScope has no scope annotation so it is NOT a CDI bean.
 * Even though GrandparentServiceWithScope (two levels up) is scoped, only the
 * direct superclass counts per CDI spec 3.1.4.
 * @Specializes here must trigger a diagnostic error.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithGrandparentBeanOnly extends MiddleServiceNoScope {
    @Override
    public String greet() { return "Custom Hello from grandchild"; }
}
