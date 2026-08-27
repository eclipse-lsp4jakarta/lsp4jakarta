package io.openliberty.sample.jakarta.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

/**
 * Invalid: Lifecycle callback interceptor methods declared in a target class
 * must have the signature void <METHOD>(). These methods violate that rule:
 * - postInit: has an InvocationContext parameter (should have no params)
 * - preCleanup: returns a non-void type (should return void)
 * - aroundInit: has a parameter (wrong signature for target class)
 */
public class InvalidLifecycleCallbackInTargetClass {

    // Invalid: @PostConstruct in target class must have no parameters
    @PostConstruct
    public void postInit(InvocationContext ctx) {
    }

    // Invalid: @PreDestroy in target class must return void
    @PreDestroy
    public String preCleanup() {
        return "done";
    }

    // Invalid: @AroundConstruct in target class must have no parameters and return void
    @AroundConstruct
    public void aroundInit(InvocationContext ctx) throws Exception {
    }

}
