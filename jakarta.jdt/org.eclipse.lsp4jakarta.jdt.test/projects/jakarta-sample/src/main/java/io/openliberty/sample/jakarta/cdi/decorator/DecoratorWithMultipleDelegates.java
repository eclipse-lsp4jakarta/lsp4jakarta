package io.openliberty.sample.jakarta.cdi.decorator;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

// Invalid: Decorator with multiple @Delegate injection points
// Should trigger diagnostic: A decorator must declare exactly one injection point annotated with @Delegate, but found 2.
@Decorator
@Dependent
public class DecoratorWithMultipleDelegates implements PaymentService {
    
    @Inject
    @Delegate
    private PaymentService delegateA;
    
    @Inject
    @Delegate
    private PaymentService delegateB;
    
    @Override
    public void processPayment(double amount) {
        // Implementation
    }
}
