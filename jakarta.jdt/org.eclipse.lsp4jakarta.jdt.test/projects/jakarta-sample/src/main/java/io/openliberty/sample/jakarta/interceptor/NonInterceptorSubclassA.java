package io.openliberty.sample.jakarta.interceptor;

/**
 * Non-interceptor subclass A of SharedAncestorWithAroundConstruct.
 * Neither this class nor its sibling (NonInterceptorSubclassB) is annotated
 * with @Interceptor, so the ancestor's @AroundConstruct remains invalid.
 */
public class NonInterceptorSubclassA extends SharedAncestorWithAroundConstruct {
}
