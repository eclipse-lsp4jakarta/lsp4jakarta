package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * A CDI bean with @ApplicationScoped scope annotation.
 * Used as the grandparent in the grandparent-only scope test.
 */
@ApplicationScoped
public class GrandparentServiceWithScope {
    public String greet() { return "Hello from grandparent"; }
}
