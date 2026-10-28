package io.openliberty.sample.jakarta.cdi.decorator.assignabletype;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Valid: delegate type Processor<String> matches the decorated type Processor<String>.
 * Per CDI 3.0 spec section 8.1.3, this is a correct decorator — the type parameters match exactly.
 *
 * Should NOT trigger any diagnostic.
 */
@Decorator
@Dependent
public class DecoratorWithMatchingTypeParam implements Processor<String> {

    @Inject
    @Delegate
    private Processor<String> delegate;  // VALID: same type parameter String

    @Override
    public void process(String input) {
        delegate.process(input);
    }
}
