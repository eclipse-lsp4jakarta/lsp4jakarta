package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Superclass of an interceptor class with valid lifecycle callback method
 * signatures — no InvalidLifecycleCallbackInterceptorMethodSignature diagnostic
 * must fire when the subclass file is opened.
 */
class InterceptorSuperClassValidBase {

    // Makes this superclass interceptor-referenced so AnnotationDiagnosticsParticipant
    // skips @PreDestroy/@PostConstruct validation on it
    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

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
}

/**
 * Interceptor subclass — extends InterceptorSuperClassValidBase.
 */
@Monitored
@Interceptor
public class InterceptorSubClassWithValidSuperClass extends InterceptorSuperClassValidBase {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}
