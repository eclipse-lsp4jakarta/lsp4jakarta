package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;

// Test case 1: Singleton with invalid scope (RequestScoped) - should report error
@Singleton
@RequestScoped
public class SingletonSessionBean {
}
