package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.ejb.Singleton;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

// Invalid: @Singleton with @Decorator
@Singleton
@Decorator
class InvalidSingletonWithDecorator implements EjbDecoratorService {
    @Inject @Delegate
    private EjbDecoratorService delegate;

    @Override
    public void businessMethod() {
    }
}
