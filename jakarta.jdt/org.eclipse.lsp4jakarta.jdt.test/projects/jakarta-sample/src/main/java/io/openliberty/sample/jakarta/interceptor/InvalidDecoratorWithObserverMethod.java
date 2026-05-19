package io.openliberty.sample.jakarta.interceptor;

import jakarta.decorator.Decorator;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

@Decorator
public class InvalidDecoratorWithObserverMethod {

    public InvalidDecoratorWithObserverMethod() {
    }

    // Invalid: Decorator with @Observes parameter
    public void observerMethod(@Observes String event) {

    }

    // Invalid: Decorator with @ObservesAsync parameter
    public void observerAsyncMethod(@ObservesAsync String event) {

    }

    // Valid: Regular method without observer annotations
    public void regularMethod(String param) {

    }
}
