package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: a producer method annotated with @Specializes must not be static.
 * Expected diagnostic: InvalidSpecializesStaticProducerMethod on method name "produce"
 * Expected diagnostic: InvalidSpecializesProducerMethodNotOverriding on method name "produce"
 * (static method cannot override, and there is no @Produces in any superclass here)
 */
@ApplicationScoped
public class SpecializesStaticProducerMethod {

    @Produces
    @Specializes
    public static String produce() {
        return "specialized";
    }
}
