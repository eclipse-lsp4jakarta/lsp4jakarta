package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.interceptor.Interceptor;

// Valid: @Interceptor without session bean annotation
@Interceptor
class ValidInterceptor {
    public void intercept() {
    }
}
