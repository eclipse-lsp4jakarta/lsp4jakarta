package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Valid: @AroundConstruct declared in a superclass that is itself an interceptor
 * class (annotated with @Interceptor). The Jakarta Interceptors spec permits
 * @AroundConstruct in interceptor classes and their superclasses — the restriction
 * only applies to target classes and their superclasses.
 */
@Monitored
@Interceptor
class ValidAroundConstructInterceptorSuperclass {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}

/**
 * Valid: An interceptor subclass that extends ValidAroundConstructInterceptorSuperclass.
 * The @AroundConstruct in the superclass is valid because the superclass is an
 * interceptor class, not a target class.
 */
@Monitored
@Interceptor
public class ValidAroundConstructInInterceptorSuperclass extends ValidAroundConstructInterceptorSuperclass {
    // Inherits the @AroundConstruct method from the interceptor superclass — valid.
}
