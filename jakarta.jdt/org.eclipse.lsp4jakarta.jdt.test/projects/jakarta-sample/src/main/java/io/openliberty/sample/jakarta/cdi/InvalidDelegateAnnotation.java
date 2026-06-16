package io.openliberty.sample.jakarta.cdi;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Test class for @Delegate annotation usage - both valid and invalid scenarios.
 * Valid: @Delegate on injection points (fields, constructor parameters, initializer method parameters)
 * Invalid: @Delegate on classes, regular methods, or other locations
 */

// Invalid: @Delegate on class
@Delegate
@Decorator
@Dependent
public class InvalidDelegateAnnotation implements PaymentService {

    // Valid: @Delegate on field (injection point)
    @Inject
    @Delegate
    private PaymentService delegateField;
    
    // Valid: @Delegate on constructor parameter (injection point)
    @Inject
    public InvalidDelegateAnnotation(@Delegate PaymentService delegateParam) {
        this.delegateField = delegateParam;
    }
    
    // Valid: @Delegate on initializer method parameter (injection point)
    @Inject
    public void init(@Delegate PaymentService delegateInitParam) {
        // Initializer method
    }

    // Invalid: @Delegate on method
    @Delegate
    public void processPayment(double amount) {
        delegateField.processPayment(amount);
    }
    
    // Invalid: @Delegate on method
    @Delegate
    public String getPaymentStatus() {
        return delegateField.getPaymentStatus();
    }
}

interface PaymentService {
    void processPayment(double amount);
    String getPaymentStatus();
}