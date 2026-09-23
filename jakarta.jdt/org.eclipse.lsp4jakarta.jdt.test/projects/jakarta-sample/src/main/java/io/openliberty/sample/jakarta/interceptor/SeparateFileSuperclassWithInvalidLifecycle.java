package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Non-interceptor superclass extended by SeparateFileInterceptorSubclassForLifecycle
 * (a separate file). Because an @Interceptor subclass exists in another file, the
 * ITypeHierarchy subtype search must trigger lifecycle callback signature validation
 * on these methods.
 *
 * All three methods below have invalid signatures — the diagnostic must fire for each.
 */
public class SeparateFileSuperclassWithInvalidLifecycle {

    // Invalid: wrong param type — @PreDestroy
    @PreDestroy
    public void preDestroyWrongParam(String name) throws Exception { }

    // Invalid: wrong return type — @PostConstruct
    @PostConstruct
    public String postConstructInvalidReturn(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }

    // Invalid: wrong return type — @AroundConstruct
    @AroundConstruct
    public String aroundConstructInvalidReturn(InvocationContext ctx) throws Exception {
        return (String) ctx.proceed();
    }
}
