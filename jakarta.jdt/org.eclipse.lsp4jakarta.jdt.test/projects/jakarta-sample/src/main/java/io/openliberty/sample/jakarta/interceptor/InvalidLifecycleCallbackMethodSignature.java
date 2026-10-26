package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Test resource for lifecycle callback interceptor method signature validation.
 * Valid signatures: void <METHOD>(InvocationContext) or Object <METHOD>(InvocationContext).
 */
@Monitored
@Interceptor
public class InvalidLifecycleCallbackMethodSignature {

    // ── @PreDestroy ──────────────────────────────────────────────────────────

    // Invalid 1: void return + wrong parameter type (String)
    @PreDestroy
    public void preDestroyVoidWrongParam(String name) throws Exception { }

    // Invalid 2: Object return + wrong parameter type (String)
    @PreDestroy
    public Object preDestroyObjectWrongParam(String name) throws Exception {
        return name;
    }

    // Invalid 3: String return + InvocationContext parameter
    @PreDestroy
    public String preDestroyInvalidReturnType(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }

    // ── @PostConstruct ───────────────────────────────────────────────────────

    // Invalid 4: void return + wrong parameter type (String)
    @PostConstruct
    public void postConstructVoidWrongParam(String name) throws Exception { }

    // Invalid 5: Object return + wrong parameter type (String)
    @PostConstruct
    public Object postConstructObjectWrongParam(String name) throws Exception {
        return name;
    }

    // Invalid 6: String return + InvocationContext parameter
    @PostConstruct
    public String postConstructInvalidReturnType(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }

    // ── @AroundConstruct ─────────────────────────────────────────────────────

    // Invalid 7: void return + wrong parameter type (String)
    @AroundConstruct
    public void aroundConstructVoidWrongParam(String name) throws Exception { }

    // Invalid 8: Object return + wrong parameter type (String)
    @AroundConstruct
    public Object aroundConstructObjectWrongParam(String name) throws Exception {
        return name;
    }

    // Invalid 9: String return + InvocationContext parameter
    @AroundConstruct
    public String aroundConstructInvalidReturnType(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }

    // ── Multiple parameters (invalid: spec requires exactly one InvocationContext) ──

    // Invalid 10: two parameters — @PreDestroy
    @PreDestroy
    public void preDestroyMultipleParams(InvocationContext ctx, InvocationContext ctx2) throws Exception {
        ctx.proceed();
    }

    // Invalid 11: two parameters — @PostConstruct
    @PostConstruct
    public void postConstructMultipleParams(InvocationContext ctx, InvocationContext ctx2) throws Exception {
        ctx.proceed();
    }

    // Invalid 12: two parameters — @AroundConstruct
    @AroundConstruct
    public void aroundConstructMultipleParams(InvocationContext ctx, InvocationContext ctx2) throws Exception {
        ctx.proceed();
    }

}
