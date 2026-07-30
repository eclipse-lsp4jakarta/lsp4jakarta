package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.ejb.Stateful;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

// Invalid: @Stateful with @Decorator
@Stateful
@Decorator
class InvalidStatefulWithDecorator {
    @Inject @Delegate
    private Object delegate;

    public void businessMethod() {
    }
}
