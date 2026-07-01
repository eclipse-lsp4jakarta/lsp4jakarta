package io.openliberty.sample.jakarta.cdi.decorator;

import jakarta.decorator.Decorator;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

// Invalid: Decorator with no @Delegate injection point
// Should trigger diagnostic: A decorator must declare exactly one injection point annotated with @Delegate.
@Decorator
@Dependent
public class DecoratorWithNoDelegate implements PaymentService {
    
    @Inject
    private Logger logger;
    
    @Override
    public void processPayment(double amount) {
        logger.log("Processing payment: " + amount);
    }
}
