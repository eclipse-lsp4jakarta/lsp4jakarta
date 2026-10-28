package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

/**
 * Generic interface extending two other generic interfaces. Used as a delegate type that
 * implements or extends more than one decorated type.
 */
public interface Pipeline<I, O> extends Processor<I>, Converter<I, O> {
}
