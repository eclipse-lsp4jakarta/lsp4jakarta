package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.ejb.Singleton;
import jakarta.interceptor.Interceptor;

// Invalid: @Singleton with @Interceptor
@Singleton
@Interceptor
class InvalidSingletonWithInterceptor {
    public void businessMethod() {
    }
}
