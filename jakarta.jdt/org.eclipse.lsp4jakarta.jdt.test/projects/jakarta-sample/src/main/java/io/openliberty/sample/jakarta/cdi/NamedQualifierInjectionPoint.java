package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Test class for @Named qualifier validation on injection points.
 * According to CDI spec section 3.9:
 * - @Named on field injection without value is VALID (field name is assumed)
 * - @Named on constructor/method parameters without value is INVALID (definition error)
 */
@ApplicationScoped
public class NamedQualifierInjectionPoint {
    
    // VALID: Field injection with @Named without value - field name "paymentService" is assumed
    @Inject
    @Named
    private String paymentService;
    
    // VALID: Field injection with @Named with value
    @Inject
    @Named("customName")
    private String orderService;
    
    // INVALID: Constructor parameter with @Named without value
    // This should trigger a diagnostic error
    @Inject
    public NamedQualifierInjectionPoint(@Named String greeting) {
        System.out.println(greeting);
    }
    
    // INVALID: Method parameter with @Named without value
    // This should trigger a diagnostic error
    @Inject
    public void setGreeting(@Named String greeting) {
        System.out.println(greeting);
    }
    
    // INVALID: Initializer method with @Named without value on parameter
    // This should trigger a diagnostic error
    @Inject
    public void initialize(@Named String config) {
        System.out.println(config);
    }
}