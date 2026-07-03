package io.openliberty.sample.jakarta.cdi.decorator;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Invalid: Delegate type (Logger) doesn't implement the decorated type (PaymentService)
 * Should trigger diagnostic: The delegate type must implement or extend to the decorator
 */
@Decorator
@Dependent
class InvalidDelegateType implements PaymentService {
    
    @Inject
    @Delegate
    private Logger delegate;  // Logger doesn't implement PaymentService
    
    @Override
    public void processPayment(double amount) {
        // This won't work
    }
}

/**
 * Valid: Delegate type (PaymentService) matches the decorated type
 * Should NOT trigger diagnostic
 */
@Decorator
@Dependent
class ValidDelegateType implements PaymentService {
    
    @Inject
    @Delegate
    private PaymentService delegate;  // PaymentService implements PaymentService
    
    @Override
    public void processPayment(double amount) {
        delegate.processPayment(amount);
    }
}

/**
 * Valid: Delegate type (PaymentServiceImpl) extends the decorated type (PaymentService)
 * Should NOT trigger diagnostic
 */
@Decorator
@Dependent
class ValidDelegateSubtype implements PaymentService {
    
    @Inject
    @Delegate
    private PaymentServiceImpl delegate;  // PaymentServiceImpl implements PaymentService
    
    @Override
    public void processPayment(double amount) {
        delegate.processPayment(amount);
    }
}

/**
 * Invalid: Delegate type (String) doesn't implement the decorated type (PaymentService)
 * Should trigger diagnostic
 */
@Decorator
@Dependent
class InvalidDelegateTypePrimitive implements PaymentService {
    
    @Inject
    @Delegate
    private String delegate;  // String doesn't implement PaymentService
    
    @Override
    public void processPayment(double amount) {
        // This won't work
    }
}

// Helper class for testing
class PaymentServiceImpl implements PaymentService {
    @Override
    public void processPayment(double amount) {
        // Implementation
    }
}

// Made with Bob
