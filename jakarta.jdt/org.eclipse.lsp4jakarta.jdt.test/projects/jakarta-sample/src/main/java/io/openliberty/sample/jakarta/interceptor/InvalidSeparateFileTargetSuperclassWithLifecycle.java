package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.InvocationContext;

/**
 * Invalid: A non-interceptor superclass whose subclass is also a non-interceptor (target class)
 * defined in a separate source file (InvalidSeparateFileTargetSubclassForLifecycle.java).
 *
 * According to Jakarta Interceptors 2.0, lifecycle callback interceptor methods declared in a
 * target class or in a superclass of a target class must have the signature void <METHOD>().
 * Methods declaring parameters or non-void return types must trigger
 * InvalidLifecycleCallbackMethodSignatureInTargetClass.
 */
public class InvalidSeparateFileTargetSuperclassWithLifecycle {

    @PostConstruct
    public void postConstructWithParam(InvocationContext ctx) {
    }

    @PreDestroy
    public String preDestroyWithNonVoidReturn() {
        return "done";
    }
}
