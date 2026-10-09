package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Valid: @AroundConstruct declared in a non-interceptor superclass whose
 * @Interceptor-annotated subclass lives in the SAME compilation unit.
 *
 * hasInterceptorSubclass() discovers same-file @Interceptor subclasses, so the
 * diagnostic is suppressed on the superclass.
 */
class SameFileSuperclassWithAroundConstruct {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}

/**
 * The @Interceptor subclass is in the same file as the superclass above.
 */
@Monitored
@Interceptor
public class SameFileInterceptorSubclassWithAroundConstruct extends SameFileSuperclassWithAroundConstruct {
}
