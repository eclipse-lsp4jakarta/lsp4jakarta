package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Valid: interceptor methods call proceed() and the call is wrapped
 * in a try/catch/finally block.
 */
public class ValidInterceptorProceedInTryCatch {

    @AroundConstruct
    public Object aroundConstruct(InvocationContext ctx) throws Exception {
        try {
            return ctx.proceed();
        } finally {
            System.out.println("aroundConstruct completed");
        }
    }

    @AroundTimeout
    public Object aroundTimeout(InvocationContext ctx) throws Exception {
        try {
            return ctx.proceed();
        } catch (Exception ex) {
            throw ex;
        }
    }

    @PostConstruct
    public void postConstruct(InvocationContext ctx) throws Exception {
        try {
            ctx.proceed();
        } finally {
            System.out.println("postConstruct completed");
        }
    }

    @PreDestroy
    public void preDestroy(InvocationContext ctx) throws Exception {
        try {
            ctx.proceed();
        } catch (Exception ex) {
            throw ex;
        }
    }

    // Corner case: a different proceed() (not InvocationContext) — should NOT warn
    static class Workflow {
        void proceed() { }
    }

    @AroundInvoke
    public Object differentProceed(InvocationContext ctx) throws Exception {
        new Workflow().proceed(); // not InvocationContext.proceed()
        try {
            return ctx.proceed();
        } catch (Exception ex) {
            throw ex;
        }
    }
}
