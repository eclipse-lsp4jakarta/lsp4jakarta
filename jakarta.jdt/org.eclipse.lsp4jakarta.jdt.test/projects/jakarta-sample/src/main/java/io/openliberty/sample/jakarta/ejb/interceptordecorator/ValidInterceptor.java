package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import io.openliberty.sample.jakarta.interceptor.Monitored;
import jakarta.interceptor.Interceptor;

// Valid: @Interceptor without session bean annotation
@Interceptor
@Monitored
class ValidInterceptor {
    public void intercept() {
    }
}
