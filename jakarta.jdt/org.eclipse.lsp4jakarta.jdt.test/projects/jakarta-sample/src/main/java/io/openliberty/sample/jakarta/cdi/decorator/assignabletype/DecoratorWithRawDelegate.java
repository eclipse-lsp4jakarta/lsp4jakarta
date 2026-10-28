package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Test cases for a raw delegate type. Per CDI 3.0 spec section 8.1.3, the delegate type must
 * implement or extend every decorated type with exactly the same type parameters, so a raw
 * Processor delegate does not satisfy a decorated type of Processor<String>.
 */

/** Invalid: the delegate is the raw type Processor but the decorated type is Processor<String>. */
@Decorator
@Dependent
@SuppressWarnings("rawtypes")
abstract class RawDelegateForParameterizedDecoratedType implements Processor<String> {
    @Inject
    @Delegate
    private Processor delegate;
}

/** Invalid: the raw delegate on an initializer method parameter. */
@Decorator
@Dependent
@SuppressWarnings("rawtypes")
abstract class RawMethodDelegateForParameterizedDecoratedType implements Processor<String> {
    @Inject
    public void setDelegate(@Delegate Processor delegate) {
    }
}

/** Valid: the decorated type and the delegate are both raw. */
@Decorator
@Dependent
@SuppressWarnings("rawtypes")
abstract class RawDelegateForRawDecoratedType implements Processor {
    @Inject
    @Delegate
    private Processor delegate;
}
