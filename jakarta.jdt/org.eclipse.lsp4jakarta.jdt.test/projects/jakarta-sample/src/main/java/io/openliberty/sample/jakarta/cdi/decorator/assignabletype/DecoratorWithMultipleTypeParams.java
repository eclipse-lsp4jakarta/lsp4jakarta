package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Test cases for decorators implementing an interface with a list of type arguments
 * (Converter<I, O>). Per CDI 3.0 spec section 8.1.3, every type argument of the delegate
 * type must match the corresponding type argument of the decorated type.
 */

/** Valid: both type arguments match, in the same order. */
@Decorator
@Dependent
abstract class ValidMultipleTypeParams implements Converter<String, Integer> {
    @Inject
    @Delegate
    private Converter<String, Integer> delegate;
}

/** Valid: fully qualified names in the implements clause resolve to the same types as simple names. */
@Decorator
@Dependent
abstract class ValidQualifiedTypeParams implements Converter<java.lang.String, java.lang.Integer> {
    @Inject
    @Delegate
    private Converter<String, Integer> delegate;
}

/** Invalid: the second type argument differs (Integer vs Object). */
@Decorator
@Dependent
abstract class SecondTypeParamMismatch implements Converter<String, Integer> {
    @Inject
    @Delegate
    private Converter<String, Object> delegate;
}

/** Invalid: the first type argument differs (String vs Object). */
@Decorator
@Dependent
abstract class FirstTypeParamMismatch implements Converter<String, Integer> {
    @Inject
    @Delegate
    private Converter<Object, Integer> delegate;
}

/** Invalid: the same type arguments in a different order. */
@Decorator
@Dependent
abstract class SwappedTypeParams implements Converter<String, Integer> {
    @Inject
    @Delegate
    private Converter<Integer, String> delegate;
}
