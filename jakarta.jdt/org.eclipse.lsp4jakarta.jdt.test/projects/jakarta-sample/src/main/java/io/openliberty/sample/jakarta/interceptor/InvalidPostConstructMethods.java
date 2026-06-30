package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
public abstract class InvalidPostConstructMethods {

	@PostConstruct
    public final Object logFinal(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

	@PostConstruct
    public abstract Object logAbstract(InvocationContext ctx) throws Exception;

	@PostConstruct
    public static Object logStatic(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

	@PostConstruct
    public Object logValid(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}


