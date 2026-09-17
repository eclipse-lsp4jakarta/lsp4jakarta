package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Invalid: @AroundConstruct declared in a non-interceptor class that is the
 * common ancestor of TWO non-interceptor subclasses defined in separate files
 * (NonInterceptorSubclassA.java and NonInterceptorSubclassB.java).
 *
 * Neither subclass is annotated with @Interceptor, so the project-wide scan
 * must NOT suppress the diagnostic here. The count in the nameCount map will
 * reach 2 (one merge per non-interceptor subclass visit — but only @Interceptor
 * classes drive the scan, so in practice no entry is added at all).
 * An InvalidAroundConstructInTargetClass diagnostic should be reported.
 */
public class SharedAncestorWithAroundConstruct {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
