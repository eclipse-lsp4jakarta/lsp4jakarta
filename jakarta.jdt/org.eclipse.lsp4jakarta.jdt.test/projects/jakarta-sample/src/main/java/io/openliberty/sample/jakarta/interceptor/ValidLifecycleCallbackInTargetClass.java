package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Valid: Lifecycle callback interceptor methods declared in a target class
 * have the correct signature void <METHOD>() (no parameters, void return type).
 */
public class ValidLifecycleCallbackInTargetClass {

    // Valid: @PostConstruct in target class - void, no parameters
    @PostConstruct
    public void postInit() {
    }

    // Valid: @PreDestroy in target class - void, no parameters
    @PreDestroy
    public void preCleanup() {
    }

}
