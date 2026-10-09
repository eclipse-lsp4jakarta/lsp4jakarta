package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.Interceptor;

/**
 * Valid: An interceptor class that extends SeparateFileSuperclassWithAroundConstruct,
 * which lives in a separate source file. The @AroundConstruct method in the
 * superclass is valid because this subclass is annotated with @Interceptor.
 *
 * No InvalidAroundConstructInTargetClass diagnostic should be reported on either
 * the superclass file or this subclass file.
 */
@Monitored
@Interceptor
public class SeparateFileInterceptorSubclass extends SeparateFileSuperclassWithAroundConstruct {
    // Inherits the @AroundConstruct method from the superclass — valid because
    // this class is annotated with @Interceptor.
}
