package io.openliberty.sample.jakarta.cdi;

import io.openliberty.sample.jakarta.cdi.decorator.PaymentService;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

// Valid: @Decorator class with a single @Inject @Delegate field
// Should NOT trigger any InvalidDelegateOutsideDecorator diagnostic
@Decorator
@Dependent
public class ValidDecoratorWithDelegate implements PaymentService {

    @Inject
    @Delegate
    private PaymentService delegate;

    @Override
    public void processPayment(double amount) {
        delegate.processPayment(amount);
    }

}
