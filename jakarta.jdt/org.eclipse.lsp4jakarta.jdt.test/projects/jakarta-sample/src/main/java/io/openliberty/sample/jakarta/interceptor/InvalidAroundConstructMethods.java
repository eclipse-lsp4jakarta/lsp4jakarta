package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

public abstract class InvalidAroundConstructMethods {

	@AroundConstruct
    public final Object logFinal(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    @AroundConstruct
    public abstract Object logAbstract(InvocationContext ctx) throws Exception;

    @AroundConstruct
    public static Object logStatic(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
    
    @AroundConstruct
    public static final Object logMulipleModifiers(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    @AroundConstruct
    public Object logValid(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}


