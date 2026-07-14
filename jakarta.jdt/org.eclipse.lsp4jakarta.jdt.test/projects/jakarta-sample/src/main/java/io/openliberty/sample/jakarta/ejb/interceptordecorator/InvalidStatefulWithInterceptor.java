package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.ejb.Stateful;
import jakarta.interceptor.Interceptor;

// Invalid: @Stateful with @Interceptor
@Stateful
@Interceptor
class InvalidStatefulWithInterceptor {
    public void businessMethod() {
    }
}
