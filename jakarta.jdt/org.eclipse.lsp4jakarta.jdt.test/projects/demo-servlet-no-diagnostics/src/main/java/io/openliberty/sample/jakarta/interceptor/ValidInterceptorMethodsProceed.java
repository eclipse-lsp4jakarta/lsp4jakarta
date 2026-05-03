package io.openliberty.sample.jakarta.interceptor;

import jakarta.fake.AroundInvoke;
import jakarta.fake.AroundConstruct;
import jakarta.fake.AroundTimeout;
import jakarta.fake.InvocationContext;
import jakarta.fake.PostConstruct;
import jakarta.fake.PreDestroy;

public class ValidInterceptorMethodsProceed {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        System.out.println("Before method: " + ctx.getMethod().getName());
        Object result = ctx.proceed(); 
        System.out.println("After method: " + ctx.getMethod().getName());
        return result;
    }

    @AroundConstruct
    public Object aroundConstruct(InvocationContext ctx) throws Exception {
        System.out.println("Around construct");
        ctx.getConstructor();
        ctx.proceed();
        ctx.getMethod();
        return ctx.proceed(); 
    }

    @AroundTimeout
    public Object aroundTimeout(InvocationContext ctx) throws Exception {
        System.out.println("Around timeout");
        return ctx.proceed(); 
    }

    @PostConstruct
    public void init(InvocationContext ctx) throws Exception {
        System.out.println("PostConstruct called for: " + ctx.getTarget());
        ctx.proceed(); 
    }

    @PreDestroy
    public void cleanup(InvocationContext ctx) throws Exception {
        System.out.println("PreDestroy called for: " + ctx.getTarget());
        ctx.proceed(); 
    }
}

