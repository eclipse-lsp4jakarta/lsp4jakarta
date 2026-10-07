package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Invalid: @AroundConstruct declared in a superclass of a target class.
 * This violates the Jakarta Interceptors 2.0 spec which states that
 * around-construct interceptor methods must not be declared in the target
 * class or in its superclasses.
 */
public class InvalidAroundConstructInSuperclass {

    @AroundConstruct
    public Object construct(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}

/**
 * Target class that extends InvalidAroundConstructInSuperclass. The
 * @AroundConstruct in the superclass is the violation — it is declared
 * in the superclass of this target class.
 */
class TargetClassExtendingSuperclassWithAroundConstruct extends InvalidAroundConstructInSuperclass {
    // No interceptor annotation here
}
