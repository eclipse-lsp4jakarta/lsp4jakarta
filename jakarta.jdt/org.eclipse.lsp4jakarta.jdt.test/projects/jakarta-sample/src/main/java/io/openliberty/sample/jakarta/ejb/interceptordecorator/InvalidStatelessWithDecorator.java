package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.ejb.Stateless;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

// Invalid: @Stateless with @Decorator
@Stateless
@Decorator
class InvalidStatelessWithDecorator implements EjbDecoratorService {
    @Inject @Delegate
    private EjbDecoratorService delegate;

    @Override
    public void businessMethod() {
    }
}
