package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Invalid: delegate type Processor<Object> does not match the decorated type Processor<String>.
 * Per CDI 3.0 spec section 8.1.3, the delegate type must implement or extend every decorated type
 * with exactly the same type parameters.
 *
 * Should trigger: InvalidDecoratorDelegateTypeAssignability
 */
@Decorator
@Dependent
public class DecoratorWithTypeParamMismatch implements Processor<String> {

    @Inject
    @Delegate
    private Processor<Object> delegate;  // INVALID: String vs Object

    @Override
    public void process(String input) {
        // This is a definition error per CDI spec
    }
}
