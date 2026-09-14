package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import io.openliberty.sample.jakarta.interceptor.Monitored;
import jakarta.ejb.Singleton;
import jakarta.interceptor.Interceptor;

// Invalid: @Singleton with @Interceptor
@Singleton
@Interceptor
@Monitored
class InvalidSingletonWithInterceptor {
    public void businessMethod() {
    }
}
