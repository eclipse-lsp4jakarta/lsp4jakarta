package io.openliberty.sample.jakarta.cdi;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;

// Test case 1: Singleton with invalid scope (RequestScoped) - should report error
@Singleton
@RequestScoped
public class SingletonSessionBean {
}

// Test case 2: Singleton with invalid scope (SessionScoped) - should report error
@Singleton
@SessionScoped
class SingletonWithSessionScope {
}

// Test case 3: Singleton with valid scope (ApplicationScoped) - should NOT report error
@Singleton
@ApplicationScoped
class SingletonWithApplicationScope {
}

// Test case 4: Singleton with valid scope (Dependent) - should NOT report error
@Singleton
@Dependent
class SingletonWithDependent {
}

// Test case 5: Singleton with no scope - should NOT report error (uses default)
@Singleton
class SingletonWithNoScope {
}

// Test case 6: Singleton with mixed valid and invalid scopes (RequestScoped + ApplicationScoped) - should report error
@Singleton
@RequestScoped
@ApplicationScoped
class SingletonWithMixedInvalidAndApplicationScoped {
}

// Test case 7: Singleton with mixed valid and invalid scopes (SessionScoped + Dependent) - should report error
@Singleton
@SessionScoped
@Dependent
class SingletonWithMixedInvalidAndDependent {
}

// Test case 8: Singleton with both valid scopes (ApplicationScoped + Dependent) - should NOT report error
@Singleton
@ApplicationScoped
@Dependent
class SingletonWithBothValidScopes {
}