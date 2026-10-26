package io.openliberty.sample.jakarta.cdi;

/**
 * A middle class that extends GrandparentServiceWithScope but has NO scope annotation.
 * This class is NOT a valid CDI bean by itself.
 * Used as the direct superclass in the grandparent-only scope test.
 */
public class MiddleServiceNoScope extends GrandparentServiceWithScope {
    @Override
    public String greet() { return "Hello from middle"; }
}
