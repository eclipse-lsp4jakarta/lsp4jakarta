package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Test resource: lifecycle callback interceptor methods with valid signatures.
 * All three lifecycle annotations use void <METHOD>(InvocationContext) or
 * Object <METHOD>(InvocationContext) — no diagnostic must fire.
 */
@Monitored
@Interceptor
public class ValidLifecycleCallbackMethodSignature {

    // Valid: void return + InvocationContext — @PreDestroy
    @PreDestroy
    public void preDestroyVoidValid(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    // Valid: Object return + InvocationContext — @PostConstruct
    @PostConstruct
    public Object postConstructObjectValid(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    // Valid: void return + InvocationContext — @AroundConstruct
    @AroundConstruct
    public void aroundConstructVoidValid(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
