package io.openliberty.sample.jakarta.cdi;

/**
 * A simple shop interface used by CDI @Typed annotation tests.
 */
public interface TypedShop<T> {
    T sell();
}
