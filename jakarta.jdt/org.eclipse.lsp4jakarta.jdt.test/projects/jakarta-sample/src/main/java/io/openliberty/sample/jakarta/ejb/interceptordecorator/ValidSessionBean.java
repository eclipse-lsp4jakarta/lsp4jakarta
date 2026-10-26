package io.openliberty.sample.jakarta.ejb.interceptordecorator;

import jakarta.ejb.Stateless;

// Valid: @Stateless without @Interceptor or @Decorator
@Stateless
class ValidSessionBean {
    public void businessMethod() {
    }
}
