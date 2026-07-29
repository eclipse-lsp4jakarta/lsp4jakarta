package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptor;

// Invalid: @Stateless with @Interceptor
@Stateless
@Interceptor
class InvalidStatelessWithInterceptor {
    public void businessMethod() {
    }
}
