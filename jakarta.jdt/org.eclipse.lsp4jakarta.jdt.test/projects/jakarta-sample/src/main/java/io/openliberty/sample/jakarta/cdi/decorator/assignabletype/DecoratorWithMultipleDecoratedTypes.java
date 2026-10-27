package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Test cases for decorators with two or more decorated types. Per CDI 3.0 spec section 8.1.3,
 * the delegate type must implement or extend every decorated type, with exactly the same
 * type parameters.
 */

/** Valid: the delegate type (Pipeline) extends both decorated types, Processor<String> and Converter<String, Integer>. */
@Decorator
@Dependent
abstract class ValidMultipleDecoratedTypes implements Processor<String>, Converter<String, Integer> {
    @Inject
    @Delegate
    private Pipeline<String, Integer> delegate;
}

/** Invalid: the delegate type (Processor) does not extend the second decorated type, Converter. */
@Decorator
@Dependent
abstract class DelegateMissingSecondDecoratedType implements Processor<String>, Converter<String, Integer> {
    @Inject
    @Delegate
    private Processor<String> delegate;
}

/** Invalid: the delegate type (Converter) does not extend the first decorated type, Processor. */
@Decorator
@Dependent
abstract class DelegateMissingFirstDecoratedType implements Processor<String>, Converter<String, Integer> {
    @Inject
    @Delegate
    private Converter<String, Integer> delegate;
}

/** Valid: the delegate extends both decorated types and its type parameters match Pipeline<String, Integer>. */
@Decorator
@Dependent
abstract class ValidMultipleDecoratedTypesMatchingParams implements Processor<String>, Pipeline<String, Integer> {
    @Inject
    @Delegate
    private Pipeline<String, Integer> delegate;
}

/** Invalid: the delegate extends both decorated types, but Pipeline<Object, Integer> differs from Pipeline<String, Integer>. */
@Decorator
@Dependent
abstract class MultipleDecoratedTypesParamMismatch implements Processor<String>, Pipeline<String, Integer> {
    @Inject
    @Delegate
    private Pipeline<Object, Integer> delegate;
}
