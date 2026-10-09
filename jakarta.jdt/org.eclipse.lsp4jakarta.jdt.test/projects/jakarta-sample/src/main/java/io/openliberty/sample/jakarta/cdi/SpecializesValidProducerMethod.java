package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

/**
 * Valid: non-static producer method with @Specializes that directly overrides
 * a @Produces method in the superclass (BaseProducer).
 * Expected: no diagnostics.
 */
@ApplicationScoped
public class SpecializesValidProducerMethod extends BaseProducer {

    @Produces
    @Specializes
    @Override
    public String produce() {
        return "specialized";
    }
}
