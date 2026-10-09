package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;

/**
 * Test resource: @AroundInvoke and @AroundTimeout methods with invalid-looking
 * signatures must NOT trigger InvalidLifecycleCallbackInterceptorMethodSignature
 * because those annotations are not lifecycle callback annotations.
 */
public class NonLifecycleInterceptorAnnotations {

    // @AroundInvoke: wrong param type — must NOT trigger lifecycle-signature diagnostic
    @AroundInvoke
    public String aroundInvokeStringWrongParam(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }

    // @AroundTimeout: wrong param type — must NOT trigger lifecycle-signature diagnostic
    @AroundTimeout
    public String aroundTimeoutStringWrongParam(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }
}
