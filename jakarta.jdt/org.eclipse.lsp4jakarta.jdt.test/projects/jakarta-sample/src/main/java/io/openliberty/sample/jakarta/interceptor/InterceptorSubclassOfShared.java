package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.Interceptor;

/**
 * An @Interceptor subclass of SharedAncestorInterceptorAndTarget.
 * Because this class is annotated with @Interceptor, the project-wide scan
 * records SharedAncestorInterceptorAndTarget's FQN in the interceptorAncestorFqns
 * set, suppressing the @AroundConstruct diagnostic on the ancestor file.
 */
@Monitored
@Interceptor
public class InterceptorSubclassOfShared extends SharedAncestorInterceptorAndTarget {
}
