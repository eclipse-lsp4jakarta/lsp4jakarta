package io.openliberty.sample.jakarta.cdi.decorator;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

// Valid: Decorator with @Delegate on constructor parameter
// Should NOT trigger any diagnostic
@Decorator
@Dependent
public class DecoratorWithDelegateOnConstructor implements PaymentService {
    
    private final PaymentService delegate;
    
    @Inject
    private Logger logger;
    
    @Inject
    public DecoratorWithDelegateOnConstructor(@Delegate PaymentService delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void processPayment(double amount) {
        logger.log("Processing payment: " + amount);
        delegate.processPayment(amount);
    }
}
