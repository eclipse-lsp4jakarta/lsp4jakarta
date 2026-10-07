package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * @Interceptor subclass of InterceptorSuperClassWithPostConstruct,
 * declared in a separate source file. Its presence triggers lifecycle
 * callback validation on the superclass. Since the superclass defines only
 * a valid @PostConstruct(InvocationContext) method, no diagnostic must fire
 * when opening InterceptorSuperClassWithPostConstruct.java.
 */
@Monitored
@Interceptor
public class InterceptorSubclassOfPostConstructSuperclass extends InterceptorSuperClassWithPostConstruct {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}
