package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

/**
 * Valid: non-static producer method with @Specializes that directly overrides
 * a @Produces method in the superclass (BaseProducer), WITHOUT the @Override
 * annotation.
 *
 * @Override is optional in Java — its absence does not affect whether a method
 * actually overrides its superclass counterpart. The CDI spec requires the
 * override at the language level, not the annotation level.
 *
 * Expected: no diagnostics.
 */
@ApplicationScoped
public class SpecializesValidProducerMethodNoOverrideAnnotation extends BaseProducer {

    @Produces
    @Specializes
    public String produce() {
        return "specialized";
    }
}
