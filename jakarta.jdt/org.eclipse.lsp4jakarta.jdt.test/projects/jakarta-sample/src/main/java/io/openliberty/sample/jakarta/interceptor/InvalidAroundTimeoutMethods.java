package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;

public abstract class InvalidAroundTimeoutMethods {

	@AroundTimeout
    public final Object logFinal(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    @AroundTimeout
    public abstract Object logAbstract(InvocationContext ctx) throws Exception;

    @AroundTimeout
    public static Object logStatic(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    @AroundTimeout
    public Object logValid(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}


