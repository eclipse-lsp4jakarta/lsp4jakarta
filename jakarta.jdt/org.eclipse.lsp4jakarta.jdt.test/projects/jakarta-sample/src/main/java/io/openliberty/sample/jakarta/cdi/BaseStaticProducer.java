package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.inject.Produces;

/**
 * Base class containing a static producer method.
 */
public class BaseStaticProducer {

    @Produces
    public static String produce() {
        return "baseStatic";
    }
}
