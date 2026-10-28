package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Test cases for decorators whose decorated type has an array type argument (Processor<String[]>).
 * Per CDI 3.0 spec section 8.1.3, the delegate type must use exactly the same type parameters.
 */

/** Valid: the delegate array type argument matches the decorated type argument. */
@Decorator
@Dependent
abstract class ValidArrayTypeParam implements Processor<String[]> {
    @Inject
    @Delegate
    private Processor<String[]> delegate;
}

/** Valid: primitive array type arguments match. */
@Decorator
@Dependent
abstract class ValidPrimitiveArrayTypeParam implements Processor<int[]> {
    @Inject
    @Delegate
    private Processor<int[]> delegate;
}

/** Invalid: the element type of the array differs (Object[] vs String[]). */
@Decorator
@Dependent
abstract class ArrayElementTypeMismatch implements Processor<String[]> {
    @Inject
    @Delegate
    private Processor<Object[]> delegate;
}

/** Invalid: array vs non-array type argument (String vs String[]). */
@Decorator
@Dependent
abstract class ArrayVersusNonArrayTypeParam implements Processor<String[]> {
    @Inject
    @Delegate
    private Processor<String> delegate;
}

/** Invalid: the array dimensions differ (String[][] vs String[]). */
@Decorator
@Dependent
abstract class ArrayDimensionMismatch implements Processor<String[]> {
    @Inject
    @Delegate
    private Processor<String[][]> delegate;
}
