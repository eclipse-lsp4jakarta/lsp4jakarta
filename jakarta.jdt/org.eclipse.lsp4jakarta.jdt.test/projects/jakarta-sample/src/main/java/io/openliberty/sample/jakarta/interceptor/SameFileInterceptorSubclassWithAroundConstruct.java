package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Invalid: @AroundConstruct declared in a non-interceptor superclass whose
 * @Interceptor-annotated subclass lives in the SAME compilation unit.
 *
 * The hasInterceptorSubclass scan explicitly excludes subtypes that share the
 * same ICompilationUnit, so the @Interceptor subclass below is not discovered.
 * The diagnostic must still fire on the superclass.
 */
class SameFileSuperclassWithAroundConstruct {

    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}

/**
 * The @Interceptor subclass is in the same file as the superclass above.
 * Because the subtype search filters out same-file types, the superclass
 * does NOT benefit from the interceptor-superclass suppression rule.
 */
@Monitored
@Interceptor
public class SameFileInterceptorSubclassWithAroundConstruct extends SameFileSuperclassWithAroundConstruct {
    // Subclass is a proper @Interceptor — but lives in the same compilation unit
    // as the superclass, so hasInterceptorSubclass() returns false for the superclass.
}
