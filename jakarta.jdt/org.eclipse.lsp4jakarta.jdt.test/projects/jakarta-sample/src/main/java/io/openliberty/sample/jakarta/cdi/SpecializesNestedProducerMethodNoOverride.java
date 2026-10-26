package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

public class SpecializesNestedProducerMethodNoOverride {

    @ApplicationScoped
    public static class NestedInvalidProducer {

        @Produces
        @Specializes
        public String produce() {
            return "nestedInvalid";
        }
    }
}
