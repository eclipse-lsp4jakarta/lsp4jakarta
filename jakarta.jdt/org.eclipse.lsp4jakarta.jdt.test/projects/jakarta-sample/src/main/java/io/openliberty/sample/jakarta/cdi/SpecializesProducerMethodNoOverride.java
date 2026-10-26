package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: a producer method annotated with @Specializes must directly override
 * another producer method in the superclass. This class has no superclass with
 * a @Produces method, so @Specializes is invalid.
 * Expected diagnostic: InvalidSpecializesProducerMethodNotOverriding on method name "produce"
 */
@ApplicationScoped
public class SpecializesProducerMethodNoOverride {

    @Produces
    @Specializes
    public String produce() {
        return "specialized";
    }
}
