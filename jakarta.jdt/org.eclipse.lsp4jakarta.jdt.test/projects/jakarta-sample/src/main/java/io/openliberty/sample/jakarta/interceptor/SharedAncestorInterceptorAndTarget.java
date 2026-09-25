package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Valid: @AroundConstruct declared in a non-interceptor class that is the
 * common ancestor of TWO subclasses defined in separate files:
 *   - InterceptorSubclassOfShared.java  — annotated with @Interceptor
 *   - NonInterceptorSubclassOfShared.java — NOT annotated with @Interceptor
 *
 * Because at least one subclass IS an @Interceptor, the ITypeHierarchy subtype
 * search finds it in the other file and suppresses the diagnostic.
 * No InvalidAroundConstructInTargetClass diagnostic should be reported.
 */
public class SharedAncestorInterceptorAndTarget {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
