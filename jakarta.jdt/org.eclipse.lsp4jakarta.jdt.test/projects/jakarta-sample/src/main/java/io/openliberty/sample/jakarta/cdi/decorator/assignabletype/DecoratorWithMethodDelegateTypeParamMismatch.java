package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Invalid: method-level delegate type Processor<Object> does not match the decorated type Processor<String>.
 * Per CDI 3.0 spec section 8.1.3, the delegate type must use exactly the same type parameters.
 *
 * Should trigger: InvalidDecoratorDelegateTypeAssignability on the method parameter.
 */
@Decorator
@Dependent
public class DecoratorWithMethodDelegateTypeParamMismatch implements Processor<String> {

    private Processor<Object> delegate;

    @Inject
    public void setDelegate(@Delegate Processor<Object> delegate) {  // INVALID: String vs Object
        this.delegate = delegate;
    }

    @Override
    public void process(String input) {
        // This is a definition error per CDI spec
    }
}
