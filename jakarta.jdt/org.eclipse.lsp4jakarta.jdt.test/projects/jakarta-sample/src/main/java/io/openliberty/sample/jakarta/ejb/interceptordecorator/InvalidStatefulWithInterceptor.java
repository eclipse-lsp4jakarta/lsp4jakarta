package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import io.openliberty.sample.jakarta.interceptor.Monitored;
import jakarta.ejb.Stateful;
import jakarta.interceptor.Interceptor;

// Invalid: @Stateful with @Interceptor
@Stateful
@Interceptor
@Monitored
class InvalidStatefulWithInterceptor {
    public void businessMethod() {
    }
}
