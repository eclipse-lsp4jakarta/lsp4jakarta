package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.inject.Produces;

/**
 * Grandparent class with a @Produces method.
 * Used to test that @Specializes on a method in a grandchild class that only
 * overrides the parent (which has no @Produces) is invalid.
 */
public class GrandparentProducer {

    @Produces
    public String grandParentProduce() {
        return "grandparent";
    }
}
