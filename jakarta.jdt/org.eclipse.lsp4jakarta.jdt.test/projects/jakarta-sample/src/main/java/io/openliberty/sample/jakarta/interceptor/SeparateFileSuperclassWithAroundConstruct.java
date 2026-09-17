package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Valid: @AroundConstruct declared in a non-interceptor superclass whose
 * @Interceptor-annotated subclass is defined in a SEPARATE source file
 * (SeparateFileInterceptorSubclass.java).
 *
 * The Jakarta Interceptors 2.0 spec permits @AroundConstruct in interceptor
 * classes AND their superclasses. When the interceptor subclass lives in a
 * different file, a file-only scan wrongly flags this class. The project-wide
 * scan via ProjectWideNameScanner must suppress the diagnostic here.
 */
public class SeparateFileSuperclassWithAroundConstruct {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
