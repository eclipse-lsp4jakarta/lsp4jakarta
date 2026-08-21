package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Invalid: interceptor methods call proceed() but the call is not wrapped
 * in a try/catch/finally block.
 */
public class InvalidInterceptorProceedNotInTryCatch {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        return ctx.proceed(); // No try/catch/finally
    }

    @AroundConstruct
    public Object aroundConstruct(InvocationContext ctx) throws Exception {
        return ctx.proceed(); // No try/catch/finally
    }

    @AroundTimeout
    public Object aroundTimeout(InvocationContext ctx) throws Exception {
        return ctx.proceed(); // No try/catch/finally
    }

    @PostConstruct
    public void postConstruct(InvocationContext ctx) throws Exception {
        ctx.proceed(); // No try/catch/finally
    }

    @PreDestroy
    public void preDestroy(InvocationContext ctx) throws Exception {
        ctx.proceed(); // No try/catch/finally
    }

    // Corner case: proceed() is called outside the try block — should still warn
    @AroundInvoke
    public Object proceedOutsideTry(InvocationContext ctx) throws Exception {
        Object result = ctx.proceed(); // outside the try — no protection
        try {
            System.out.println("logging");
        } finally {
            System.out.println("cleanup");
        }
        return result;
    }
}
