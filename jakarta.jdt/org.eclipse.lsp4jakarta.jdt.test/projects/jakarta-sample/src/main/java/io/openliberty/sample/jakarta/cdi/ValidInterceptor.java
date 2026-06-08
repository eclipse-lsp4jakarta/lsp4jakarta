package io.openliberty.sample.jakarta.cdi;

import jakarta.interceptor.Interceptor;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

@Interceptor
public class ValidInterceptor {
    
    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        // Valid: No disposer methods
        return context.proceed();
    }
    
    // Valid: Regular method without @Disposes
    public void cleanup(Connection conn) {
        conn.close();
    }
    
    // Helper class for testing
    static class Connection {
        public void close() {}
    }
}

// Made with Bob
