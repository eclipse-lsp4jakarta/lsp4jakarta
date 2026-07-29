package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

// Valid: @Decorator without session bean annotation
@Decorator
class ValidDecorator {
    @Inject @Delegate
    private Object delegate;

    public void decorate() {
    }
}
