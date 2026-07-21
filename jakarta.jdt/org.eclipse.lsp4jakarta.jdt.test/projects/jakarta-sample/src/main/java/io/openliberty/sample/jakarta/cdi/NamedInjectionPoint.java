package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
public class NamedInjectionPoint {
    
    // VALID: Field injection with @Named without value - field name is assumed
    @Inject
    @Named
    private String paymentService;
    
    // VALID: Field injection with @Named with value
    @Inject
    @Named("customName")
    private String orderService;
    
    // INVALID: Constructor parameter with @Named without value
    @Inject
    public NamedInjectionPoint(@Named String greeting) {
        System.out.println(greeting);
    }
    
    // VALID: Constructor parameter with @Named with value
    public static class ValidConstructor {
        @Inject
        public ValidConstructor(@Named("hello") String greeting) {
            System.out.println(greeting);
        }
    }
    
    // INVALID: Method parameter with @Named without value
    @Inject
    public void setGreeting(@Named String greeting) {
        System.out.println(greeting);
    }
    
    // VALID: Method parameter with @Named with value
    @Inject
    public void setMessage(@Named("welcome") String message) {
        System.out.println(message);
    }
    
    // INVALID: Multiple constructor parameters with @Named without value
    public static class MultipleInvalidParams {
        @Inject
        public MultipleInvalidParams(@Named String greeting, @Named String farewell) {
            System.out.println(greeting + " " + farewell);
        }
    }
    
    // VALID: Mixed - one with value, one without (but still invalid for the one without)
    public static class MixedParams {
        @Inject
        public MixedParams(@Named("hello") String greeting, @Named String farewell) {
            System.out.println(greeting + " " + farewell);
        }
    }
    
    // INVALID: Initializer method with @Named without value on parameter
    @Inject
    public void initialize(@Named String config) {
        System.out.println(config);
    }
    
    // VALID: Regular field without @Inject - @Named is not relevant here
    @Named
    private String regularField;
}