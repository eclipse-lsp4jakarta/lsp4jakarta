package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Specializes;

/**
 * Valid: BaseServiceWithDependentScope is annotated with @Dependent (a valid CDI bean).
 * @Specializes here should NOT trigger a diagnostic.
 */
@Specializes
@Dependent
public class SpecializesWithDependentScopedSuperclass extends BaseServiceWithDependentScope {
    @Override
    public String greet() { return "Custom Hello from Dependent"; }
}
