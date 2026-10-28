package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Valid: method-level delegate type Processor<String> matches the decorated type Processor<String>.
 * Per CDI 3.0 spec section 8.1.3, this is a correct decorator — the type parameters match exactly.
 *
 * Should NOT trigger any diagnostic.
 */
@Decorator
@Dependent
public class DecoratorWithMethodDelegateMatchingTypeParam implements Processor<String> {

    private Processor<String> delegate;

    @Inject
    public void setDelegate(@Delegate Processor<String> delegate) {  // VALID: same type parameter String
        this.delegate = delegate;
    }

    @Override
    public void process(String input) {
        delegate.process(input);
    }
}
