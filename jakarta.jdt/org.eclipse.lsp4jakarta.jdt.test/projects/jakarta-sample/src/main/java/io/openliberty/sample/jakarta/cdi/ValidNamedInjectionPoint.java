package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Test class with VALID uses of @Named qualifier on injection points.
 */
@ApplicationScoped
public class ValidNamedInjectionPoint {
    
    // VALID: Field injection with @Named without value - field name is assumed
    @Inject
    @Named
    private String paymentService;
    
    // VALID: Constructor parameter with @Named WITH value
    @Inject
    public ValidNamedInjectionPoint(@Named("hello") String greeting) {
        System.out.println(greeting);
    }
    
    // VALID: Method parameter with @Named WITH value
    @Inject
    public void setMessage(@Named("welcome") String message) {
        System.out.println(message);
    }
    
    // VALID: Initializer method with @Named WITH value on parameter
    @Inject
    public void initialize(@Named("config") String config) {
        System.out.println(config);
    }
}