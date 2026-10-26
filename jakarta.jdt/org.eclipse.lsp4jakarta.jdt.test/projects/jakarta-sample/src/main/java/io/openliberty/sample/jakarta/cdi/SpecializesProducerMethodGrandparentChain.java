package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: @Specializes on a producer method that overrides
 * ParentProducerNoProduces.grandParentProduce(), but ParentProducerNoProduces
 * does NOT declare @Produces on that method.
 *
 * The CDI spec requires that a @Specializes producer method directly overrides
 * a @Produces method in the immediate superclass. The @Produces annotation on
 * GrandparentProducer.grandParentProduce() does NOT satisfy this — only the
 * direct superclass (ParentProducerNoProduces) is checked, and it has no
 * @Produces on grandParentProduce().
 *
 */
@ApplicationScoped
public class SpecializesProducerMethodGrandparentChain extends ParentProducerNoProduces {

    @Produces
    @Specializes
    @Override
    public String grandParentProduce() {
        return "grandchild";
    }
}
