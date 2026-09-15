package io.openliberty.sample.jakarta.cdi.decorator;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

// Valid: Decorator with @Delegate on initializer method parameter
// Should NOT trigger any diagnostic
@Decorator
@Dependent
public class DecoratorWithDelegateOnMethod implements PaymentService {
    
    private PaymentService delegate;
    
    @Inject
    private Logger logger;
    
    @Inject
    public void setDelegate(@Delegate PaymentService delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void processPayment(double amount) {
        logger.log("Processing payment: " + amount);
        delegate.processPayment(amount);
    }
}
