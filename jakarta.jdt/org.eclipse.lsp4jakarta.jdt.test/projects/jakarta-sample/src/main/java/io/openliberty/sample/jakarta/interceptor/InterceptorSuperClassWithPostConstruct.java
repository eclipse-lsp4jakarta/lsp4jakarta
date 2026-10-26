package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Standalone superclass (in its own file) of InterceptorSubclassOfPostConstructSuperclass,
 * which is declared in a separate file and annotated with @Interceptor.
 *
 * The @PostConstruct method here uses the interceptor-valid signature
 * void(InvocationContext). Because an @Interceptor subclass exists in another
 * file, the Annotation diagnostics participant must NOT flag the InvocationContext
 * parameter, and the Interceptor diagnostics participant must NOT flag an invalid
 * signature. Opening this file must produce 0 diagnostics.
 */
public class InterceptorSuperClassWithPostConstruct {

    @PostConstruct
    public void postConstruct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
