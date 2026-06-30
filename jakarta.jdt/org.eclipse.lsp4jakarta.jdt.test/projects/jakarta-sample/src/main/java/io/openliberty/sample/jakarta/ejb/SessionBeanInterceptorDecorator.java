package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.Stateful;
import jakarta.ejb.Singleton;
import jakarta.interceptor.Interceptor;
import jakarta.decorator.Decorator;

/**
 * Test file for session beans with @Interceptor or @Decorator annotations
 */

// Invalid: @Stateless with @Interceptor
@Stateless
@Interceptor
class InvalidStatelessWithInterceptor {
    public void businessMethod() {
    }
}

// Invalid: @Stateless with @Decorator
@Stateless
@Decorator
class InvalidStatelessWithDecorator {
    public void businessMethod() {
    }
}

// Invalid: @Stateful with @Interceptor
@Stateful
@Interceptor
class InvalidStatefulWithInterceptor {
    public void businessMethod() {
    }
}

// Invalid: @Stateful with @Decorator
@Stateful
@Decorator
class InvalidStatefulWithDecorator {
    public void businessMethod() {
    }
}

// Invalid: @Singleton with @Interceptor
@Singleton
@Interceptor
class InvalidSingletonWithInterceptor {
    public void businessMethod() {
    }
}

// Invalid: @Singleton with @Decorator
@Singleton
@Decorator
class InvalidSingletonWithDecorator {
    public void businessMethod() {
    }
}

// Valid: @Interceptor without session bean annotation
@Interceptor
class ValidInterceptor {
    public void intercept() {
    }
}

// Valid: @Decorator without session bean annotation
@Decorator
class ValidDecorator {
    public void decorate() {
    }
}

// Valid: @Stateless without @Interceptor or @Decorator
@Stateless
class ValidSessionBean {
    public void businessMethod() {
    }
}
