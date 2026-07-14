package io.openliberty.sample.jakarta.cdi.decorator;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

// Valid: Decorator with exactly one @Delegate injection point
// Should NOT trigger any diagnostic
@Decorator
@Dependent
public class ValidDecorator implements PaymentService {
    
    @Inject
    @Delegate
    private PaymentService delegate;
    
    @Inject
    private Logger logger;
    
    @Override
    public void processPayment(double amount) {
        logger.log("Processing payment: " + amount);
        delegate.processPayment(amount);
    }
}
