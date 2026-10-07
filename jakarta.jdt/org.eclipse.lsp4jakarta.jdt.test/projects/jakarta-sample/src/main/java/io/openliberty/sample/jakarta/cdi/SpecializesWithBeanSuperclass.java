package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Valid: BaseServiceWithScope is annotated with @ApplicationScoped (a valid CDI bean).
 * @Specializes here should NOT trigger a diagnostic.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithBeanSuperclass extends BaseServiceWithScope {
    @Override
    public String greet() { return "Custom Hello"; }
}
