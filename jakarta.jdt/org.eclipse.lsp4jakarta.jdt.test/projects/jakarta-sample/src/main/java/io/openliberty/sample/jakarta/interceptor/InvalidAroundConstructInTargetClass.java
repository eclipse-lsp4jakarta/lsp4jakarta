package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Invalid: @AroundConstruct declared in a target class (non-interceptor).
 * This violates the Jakarta Interceptors 2.0 spec which states that
 * around-construct interceptor methods must not be declared in the target
 * class or in its superclasses.
 */
public class InvalidAroundConstructInTargetClass {

    @AroundConstruct
    public Object construct(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}
