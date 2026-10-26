package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Invalid: the interceptor method has two proceed() calls — the first is inside
 * a try/catch but the second is outside, in an if/else branch.
 * The diagnostic must fire for the unprotected second call.
 */
@Monitored
@Interceptor
public class InvalidInterceptorTwoProceedCalls {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        Object result = null;
        try {
            result = ctx.proceed(); // first call — inside try, protected
        } catch (SomeSpecificException e) {
            // swallow and fall through to retry
        }
        if (result == null) {
            result = ctx.proceed(); // second call — outside try, unprotected
        }
        return result;
    }
}
