package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

/**
 * Generic interface used to test delegate type parameter compliance.
 */
public interface Processor<T> {
    void process(T input);
}
