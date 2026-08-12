package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Superclass of an interceptor class with invalid lifecycle callback method
 * signatures. Validated when the subclass file is opened because both are
 * declared in the same compilation unit.
 */
class InterceptorSuperClassBase {

    // Invalid: wrong param type — @PreDestroy
    @PreDestroy
    public void preDestroyWrongParam(String name) throws Exception { }

    // Invalid: wrong return type — @PostConstruct
    @PostConstruct
    public String postConstructInvalidReturn(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }
    
 // Invalid: wrong return type — @PostConstruct
    @AroundConstruct
    public String aroundConstructInvalidReturn(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }
}

/**
 * Interceptor subclass — extends InterceptorSuperClassBase. Opening this file
 * triggers validation of the superclass lifecycle callback methods above.
 */
@Monitored
@Interceptor
public class InterceptorSubClassWithInvalidSuperClass extends InterceptorSuperClassBase {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}
