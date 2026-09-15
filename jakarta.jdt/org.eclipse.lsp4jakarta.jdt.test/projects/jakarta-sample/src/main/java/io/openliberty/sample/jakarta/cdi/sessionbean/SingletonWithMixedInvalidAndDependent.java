package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.SessionScoped;

// Test case 7: Singleton with mixed invalid+valid scopes (SessionScoped + Dependent) - should report error
@Singleton
@SessionScoped
@Dependent
public class SingletonWithMixedInvalidAndDependent {
}
