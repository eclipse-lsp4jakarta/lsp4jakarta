package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Logger;
import java.util.logging.Level;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class ValidInterceptorMethodsProceed {
	
	private static final Logger LOGGER = Logger.getLogger(ValidInterceptorMethodsProceed.class.getName());

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "Before method: " + ctx.getMethod().getName());
        Object result = ctx.proceed(); 
        LOGGER.log(Level.INFO, "After method: " + ctx.getMethod().getName());
        return result;
    }

    @AroundConstruct
    public Object aroundConstruct(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "Around construct");
        ctx.getConstructor();
        ctx.proceed();
        ctx.getMethod();
        return ctx.proceed(); 
    }

    @AroundTimeout
    public Object aroundTimeout(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "Around timeout");
        return ctx.proceed(); 
    }

    @PostConstruct
    public void init(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "PostConstruct called for: " + ctx.getTarget());
        ctx.proceed(); 
    }

    @PreDestroy
    public void cleanup(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "PreDestroy called for: " + ctx.getTarget());
        ctx.proceed(); 
    }
}