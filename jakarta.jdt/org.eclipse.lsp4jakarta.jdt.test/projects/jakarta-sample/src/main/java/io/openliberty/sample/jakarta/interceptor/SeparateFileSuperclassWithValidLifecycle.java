package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Non-interceptor superclass extended by SeparateFileInterceptorSubclassValidLifecycle
 * (a separate file). Because an @Interceptor subclass exists in another file, the
 * ITypeHierarchy subtype search triggers lifecycle callback signature validation on
 * these methods. All signatures below are valid — no diagnostic must fire.
 */
public class SeparateFileSuperclassWithValidLifecycle {

    // Valid: void + InvocationContext — @PreDestroy
    @PreDestroy
    public void preDestroyValid(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    // Valid: Object + InvocationContext — @PostConstruct
    @PostConstruct
    public Object postConstructValid(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    // Valid: void + InvocationContext — @AroundConstruct
    @AroundConstruct
    public void aroundConstructValid(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
