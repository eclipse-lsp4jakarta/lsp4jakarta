package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Valid: @AroundConstruct declared in a class annotated with @Interceptor.
 * The Jakarta Interceptors spec allows @AroundConstruct only in interceptor
 * classes and/or their superclasses — not in target classes.
 */
@Monitored
@Interceptor
public class ValidAroundConstructInInterceptorClass {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
