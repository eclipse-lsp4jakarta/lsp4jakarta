package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.Dependent;

// Test case 4: Singleton with valid scope (Dependent) - should NOT report error
@Singleton
@Dependent
public class SingletonWithDependent {
}
