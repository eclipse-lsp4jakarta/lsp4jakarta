package io.openliberty.sample.jakarta.cdi.decorator;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

// Invalid: Decorator with @Delegate on both field and constructor parameter
// Should trigger diagnostic: A decorator must declare exactly one @Delegate injection point. Found 2 @Delegate injection points.
@Decorator
@Dependent
public class DecoratorWithMixedDelegates implements PaymentService {
    
    @Inject
    @Delegate
    private PaymentService delegateField;
    
    private final PaymentService delegateConstructor;
    
    @Inject
    public DecoratorWithMixedDelegates(@Delegate PaymentService delegate) {
        this.delegateConstructor = delegate;
    }
    
    @Override
    public void processPayment(double amount) {
        delegateField.processPayment(amount);
    }
}

// Made with Bob
