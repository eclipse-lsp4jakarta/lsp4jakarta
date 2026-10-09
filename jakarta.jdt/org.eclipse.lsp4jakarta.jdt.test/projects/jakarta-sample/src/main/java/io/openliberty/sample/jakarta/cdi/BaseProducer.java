package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.inject.Produces;

/**
 * Base class containing a producer method that can be specialized by a subclass.
 */
public class BaseProducer {

    /**
     * A producer method that subclasses may specialize.
     */
    @Produces
    public String produce() {
        return "base";
    }
}
