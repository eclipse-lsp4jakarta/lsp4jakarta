package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

/**
 * Three invalid producer-method specialization scenarios in one class,
 * all extending BaseProducer whose superclass producer is:
 *
 *   @Produces public String produce() { ... }
 *
 * Scenario A — same method name, different parameter list:
 *   produce(String qualifier) does NOT override produce() because the
 *   signatures differ. @Specializes is therefore invalid.
 *   Expected: InvalidSpecializesProducerMethodNotOverriding on "produce"
 *
 * Scenario B — different method name, no parameters:
 *   createValue() has no counterpart in BaseProducer at all.
 *   Expected: InvalidSpecializesProducerMethodNotOverriding on "createValue"
 *
 * Scenario C — different method name, different parameter list:
 *   build(int count) has no counterpart in BaseProducer at all.
 *   Expected: InvalidSpecializesProducerMethodNotOverriding on "build"
 */
@ApplicationScoped
public class SpecializesProducerMethodNotOverriding extends BaseProducer {

    // Same name as BaseProducer.produce() but adds a parameter — this is an overload, not an override.
    @Produces
    @Specializes
    public String produce(String qualifier) {
        return "specialized-" + qualifier;
    }

    // Completely different name, no params — no matching producer method in BaseProducer.
    @Produces
    @Specializes
    public String createValue() {
        return "created";
    }

    // Completely different name and different param — no matching producer method in BaseProducer.
    @Produces
    @Specializes
    public String build(int count) {
        return "built-" + count;
    }
}
