package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.SessionScoped;

// Test case 2: Singleton with invalid scope (SessionScoped) - should report error
@Singleton
@SessionScoped
public class SingletonWithSessionScope {
}
