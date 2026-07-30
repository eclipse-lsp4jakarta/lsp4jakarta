package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.Interceptor;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.Priority;

/**
 * Valid: Interceptor declared with @Interceptor and has @Monitored interceptor binding.
 * This should NOT trigger any diagnostic.
 */
@Monitored
@Interceptor
@Priority(2100)
public class ValidInterceptorWithBinding {
    
    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {
        Object result = ctx.proceed();
        return result;
    }
}

