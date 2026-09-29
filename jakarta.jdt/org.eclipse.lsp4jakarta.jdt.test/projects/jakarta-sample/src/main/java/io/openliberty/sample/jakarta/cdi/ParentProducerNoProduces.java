package io.openliberty.sample.jakarta.cdi;

/**
 * Middle class in the producer chain.
 * Inherits grandParentProduce() from GrandparentProducer but does NOT
 * redeclare @Produces on it. This means its grandParentProduce() method
 * is NOT itself a producer method.
 */
public class ParentProducerNoProduces extends GrandparentProducer {
    // inherits grandParentProduce() without @Produces
}
