package io.openliberty.sample.jakarta.cdi;

import jakarta.interceptor.Interceptor;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import io.openliberty.sample.jakarta.interceptor.Monitored;
import jakarta.enterprise.inject.Disposes;

@Monitored
@Interceptor
public class InterceptorWithDisposer {
    
    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        return context.proceed();
    }
    
    public void cleanup(@Disposes Connection conn) {
        conn.close();
    }
    
    static class Connection {
        public void close() {}
    }
}
