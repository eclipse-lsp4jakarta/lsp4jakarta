package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PreDestroy;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
public abstract class InvalidPreDestroyMethods {

	@PreDestroy
    public final Object logFinal(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

	@PreDestroy
    public abstract Object logAbstract(InvocationContext ctx) throws Exception;

	@PreDestroy
    public static Object logStatic(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

	@PreDestroy
    public Object logValid(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}


