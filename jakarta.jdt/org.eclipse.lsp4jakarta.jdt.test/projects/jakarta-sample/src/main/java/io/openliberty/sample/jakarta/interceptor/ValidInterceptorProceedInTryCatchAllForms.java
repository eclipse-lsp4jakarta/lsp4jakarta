package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Valid: exercises all three try-statement forms that should suppress the
 * "proceed not in try/catch" warning.  All three map to the same JDT
 * TryStatement AST node type, so the single instanceof check in ASTUtils
 * accepts all of them.
 *
 *   try/catch           — aroundTimeout, preDestroy
 *   try/finally         — aroundConstruct, postConstruct
 *   try/catch/finally   — aroundInvoke
 */
public class ValidInterceptorProceedInTryCatchAllForms {

    // try/catch
    @AroundTimeout
    public Object aroundTimeout(InvocationContext ctx) throws Exception {
        try {
            return ctx.proceed();
        } catch (Exception ex) {
            throw ex;
        }
    }

    // try/catch
    @PreDestroy
    public void preDestroy(InvocationContext ctx) throws Exception {
        try {
            ctx.proceed();
        } catch (Exception ex) {
            throw ex;
        }
    }

    // try/finally
    @AroundConstruct
    public Object aroundConstruct(InvocationContext ctx) throws Exception {
        try {
            return ctx.proceed();
        } finally {
            System.out.println("aroundConstruct completed");
        }
    }

    // try/finally
    @PostConstruct
    public void postConstruct(InvocationContext ctx) throws Exception {
        try {
            ctx.proceed();
        } finally {
            System.out.println("postConstruct completed");
        }
    }

    // try/catch/finally — all three clauses present
    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        try {
            return ctx.proceed();
        } catch (Exception ex) {
            throw ex;
        } finally {
            System.out.println("aroundInvoke completed");
        }
    }
}
