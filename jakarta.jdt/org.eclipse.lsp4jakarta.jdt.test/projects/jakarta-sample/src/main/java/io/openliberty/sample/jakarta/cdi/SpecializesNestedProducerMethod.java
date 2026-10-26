package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

public class SpecializesNestedProducerMethod {

    @ApplicationScoped
    public static class NestedSpecializedProducer extends BaseProducer {

        @Produces
        @Specializes
        @Override
        public String produce() {
            return "nestedSpecialized";
        }
    }
}
