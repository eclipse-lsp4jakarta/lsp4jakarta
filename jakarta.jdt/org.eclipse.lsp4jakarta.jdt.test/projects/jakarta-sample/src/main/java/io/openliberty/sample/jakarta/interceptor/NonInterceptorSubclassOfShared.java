package io.openliberty.sample.jakarta.interceptor;

/**
 * A non-interceptor subclass of SharedAncestorInterceptorAndTarget.
 * Even though this class is NOT annotated with @Interceptor, its sibling
 * (InterceptorSubclassOfShared) IS. The project-wide scan therefore still
 * adds the ancestor's FQN to interceptorAncestorFqns, and no diagnostic
 * is reported on the ancestor file.
 */
public class NonInterceptorSubclassOfShared extends SharedAncestorInterceptorAndTarget {
}
