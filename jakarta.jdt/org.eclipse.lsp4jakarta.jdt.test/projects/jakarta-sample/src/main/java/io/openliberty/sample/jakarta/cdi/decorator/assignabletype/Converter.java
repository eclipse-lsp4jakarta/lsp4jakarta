package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

/**
 * Generic interface with multiple type parameters used to test delegate type
 * parameter compliance for a list of type arguments.
 */
public interface Converter<I, O> {
    O convert(I input);
}
