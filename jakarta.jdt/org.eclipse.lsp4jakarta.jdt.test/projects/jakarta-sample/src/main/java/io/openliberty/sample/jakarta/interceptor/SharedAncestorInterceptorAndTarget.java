package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Valid: @AroundConstruct declared in a non-interceptor class that is the
 * common ancestor of TWO subclasses defined in separate files:
 *   - InterceptorSubclassOfShared.java  — annotated with @Interceptor
 *   - NonInterceptorSubclassOfShared.java — NOT annotated with @Interceptor
 *
 * Because at least one subclass IS an @Interceptor, the project-wide scan
 * adds this class's FQN to the interceptorAncestorFqns set, and the diagnostic
 * must be suppressed here. nameCount.merge() is called once (by the interceptor
 * subclass visit), resulting in a count of 1 for this FQN.
 * No InvalidAroundConstructInTargetClass diagnostic should be reported.
 */
public class SharedAncestorInterceptorAndTarget {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
