package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * @Interceptor subclass of SeparateFileSuperclassWithInvalidLifecycle,
 * declared in a separate source file. Its presence is discovered by
 * ITypeHierarchy, triggering lifecycle callback signature validation on
 * the superclass methods.
 */
@Monitored
@Interceptor
public class SeparateFileInterceptorSubclassForLifecycle extends SeparateFileSuperclassWithInvalidLifecycle {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}
