package io.openliberty.sample.jakarta.interceptor;

/**
 * Non-interceptor subclass B of SharedAncestorWithAroundConstruct.
 * Neither this class nor its sibling (NonInterceptorSubclassA) is annotated
 * with @Interceptor, so the ancestor's @AroundConstruct remains invalid.
 */
public class NonInterceptorSubclassB extends SharedAncestorWithAroundConstruct {
}
