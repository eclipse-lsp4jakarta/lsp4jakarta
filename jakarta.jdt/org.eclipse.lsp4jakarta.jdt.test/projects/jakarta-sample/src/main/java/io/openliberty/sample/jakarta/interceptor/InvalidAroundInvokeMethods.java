package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public abstract class InvalidAroundInvokeMethods {

	@AroundInvoke
    public final Object logFinal(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    @AroundInvoke
    public abstract Object logAbstract(InvocationContext ctx) throws Exception;

    @AroundInvoke
    public static Object logStatic(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    @AroundInvoke
    public Object logValid(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}


