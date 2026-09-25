package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Valid: @AroundConstruct declared in a non-interceptor superclass whose
 * @Interceptor-annotated subclass is defined in a SEPARATE source file
 * (SeparateFileInterceptorSubclass.java).
 *
 * The Jakarta Interceptors 2.0 spec permits @AroundConstruct in interceptor
 * classes AND their superclasses. The ITypeHierarchy subtype search discovers
 * the @Interceptor subclass in the other file and suppresses the diagnostic.
 */
public class SeparateFileSuperclassWithAroundConstruct {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
