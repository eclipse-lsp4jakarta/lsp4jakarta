package io.openliberty.sample.jakarta.cdi;

/**
 * A plain class with no CDI scope annotation.
 * Not a valid CDI bean.
 */
public class BaseServiceNoScope {
    public String greet() { return "Hello"; }
}
