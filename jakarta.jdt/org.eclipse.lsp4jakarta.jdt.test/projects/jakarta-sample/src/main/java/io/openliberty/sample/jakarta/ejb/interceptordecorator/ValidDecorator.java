package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

// Valid: @Decorator without session bean annotation
@Decorator
class ValidDecorator implements EjbDecoratorService {
    @Inject @Delegate
    private EjbDecoratorService delegate;

    @Override
    public void businessMethod() {
        delegate.businessMethod();
    }
}
