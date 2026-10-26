package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Singleton;

// Test case 5: Singleton with no scope - should NOT report error (uses default)
@Singleton
public class SingletonWithNoScope {
}
