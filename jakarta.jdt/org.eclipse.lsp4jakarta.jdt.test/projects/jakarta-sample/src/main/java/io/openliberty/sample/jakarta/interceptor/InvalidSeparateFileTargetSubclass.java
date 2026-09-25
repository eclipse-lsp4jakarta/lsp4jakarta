package io.openliberty.sample.jakarta.interceptor;

/**
 * Invalid: A target class (non-interceptor) that extends
 * InvalidSeparateFileSuperclassWithAroundConstruct, which is defined in a
 * separate source file and contains a method annotated with @AroundConstruct.
 *
 * Neither this class nor its superclass is annotated with @Interceptor, so the
 * @AroundConstruct in the superclass is a spec violation. The diagnostic must
 * fire on the superclass file (InvalidSeparateFileSuperclassWithAroundConstruct.java).
 */
public class InvalidSeparateFileTargetSubclass extends InvalidSeparateFileSuperclassWithAroundConstruct {
    // No @Interceptor annotation here — extending a non-interceptor superclass
    // that incorrectly declares @AroundConstruct.
}
