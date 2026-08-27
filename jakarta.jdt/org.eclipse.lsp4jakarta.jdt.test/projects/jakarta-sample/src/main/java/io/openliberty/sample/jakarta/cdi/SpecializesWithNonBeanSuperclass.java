package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: BaseServiceNoScope has no scope annotation, so it is not a CDI bean.
 * @Specializes here should trigger a diagnostic error.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithNonBeanSuperclass extends BaseServiceNoScope {
    @Override
    public String greet() { return "Custom Hello"; }
}
