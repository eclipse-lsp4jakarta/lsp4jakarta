package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import io.openliberty.sample.jakarta.interceptor.Monitored;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptor;

// Invalid: @Stateless with @Interceptor
@Stateless
@Interceptor
@Monitored
class InvalidStatelessWithInterceptor {
    public void businessMethod() {
    }
}
