package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Invalid: @AroundConstruct declared in a non-interceptor superclass whose only
 * known subclass (InvalidSeparateFileTargetSubclass.java) is ALSO a non-interceptor
 * class defined in a separate source file.
 *
 * The Jakarta Interceptors 2.0 spec forbids @AroundConstruct in target classes
 * and their superclasses. Since no @Interceptor-annotated class extends this
 * superclass, the project-wide scan must NOT suppress the diagnostic here.
 * An InvalidAroundConstructInTargetClass diagnostic should be reported.
 */
public class InvalidSeparateFileSuperclassWithAroundConstruct {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
