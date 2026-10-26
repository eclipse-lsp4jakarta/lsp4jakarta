package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * @Interceptor subclass of SeparateFileSuperclassWithValidLifecycle,
 * declared in a separate source file. Its presence is discovered by
 * ITypeHierarchy, triggering lifecycle callback signature validation on
 * the superclass methods. Since all signatures are valid, no diagnostic
 * must fire.
 */
@Monitored
@Interceptor
public class SeparateFileInterceptorSubclassValidLifecycle extends SeparateFileSuperclassWithValidLifecycle {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}
