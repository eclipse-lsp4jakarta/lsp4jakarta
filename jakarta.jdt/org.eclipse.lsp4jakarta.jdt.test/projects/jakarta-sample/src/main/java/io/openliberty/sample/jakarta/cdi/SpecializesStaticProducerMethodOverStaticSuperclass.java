package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

@ApplicationScoped
public class SpecializesStaticProducerMethodOverStaticSuperclass extends BaseStaticProducer {

    @Produces
    @Specializes
    public static String produce() {
        return "specializedStatic";
    }
}
