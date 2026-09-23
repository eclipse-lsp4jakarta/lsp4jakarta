package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Valid: Extends BaseServiceWithScope (a CDI bean) AND implements an interface.
 * The direct superclass is a valid bean, so @Specializes should NOT trigger a diagnostic.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithBeanSuperclassAndInterface extends BaseServiceWithScope implements BaseServiceInterface {
    @Override
    public String greet() { return "Custom Hello"; }
}
