package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.Interceptor;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.Priority;

/**
 * Invalid: Interceptor declared with @Interceptor but missing interceptor binding annotation.
 * This should trigger diagnostic: InvalidInterceptorMissingInterceptorBinding
 */
@Interceptor
@Priority(2100)
public class InvalidInterceptorMissingBinding {
    
    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {
        Object result = ctx.proceed();
        return result;
    }
}

