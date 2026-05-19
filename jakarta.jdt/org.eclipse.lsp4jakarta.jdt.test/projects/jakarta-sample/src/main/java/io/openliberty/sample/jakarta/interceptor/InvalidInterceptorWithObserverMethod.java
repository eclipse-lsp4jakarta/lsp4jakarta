package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.Interceptor;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

@Interceptor
public class InvalidInterceptorWithObserverMethod {

    public InvalidInterceptorWithObserverMethod() {
    }

    // Invalid: Interceptor with @Observes parameter
    public void observerMethod(@Observes String event) {

    }

    // Invalid: Interceptor with @ObservesAsync parameter
    public void observerAsyncMethod(@ObservesAsync String event) {

    }

    // Invalid: Interceptor with both @Observes and @ObservesAsync parameters
    public void observerBothMethod(@Observes String event1, @ObservesAsync String event2) {

    }

    // Valid: Regular method without observer annotations
    public void regularMethod(String param) {

    }
}
