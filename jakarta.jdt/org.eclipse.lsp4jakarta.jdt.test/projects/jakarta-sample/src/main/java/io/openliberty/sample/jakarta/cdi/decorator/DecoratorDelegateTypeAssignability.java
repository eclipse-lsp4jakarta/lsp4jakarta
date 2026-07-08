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

/**
 * Invalid: Delegate type on method parameter (Logger) doesn't implement the decorated type (PaymentService)
 * Should trigger diagnostic: The delegate type must implement or extend to the decorator
 */
@Decorator
@Dependent
class InvalidDelegateTypeOnMethod implements PaymentService {
    
    private Logger delegate;
    
    @Inject
    public void setDelegate(@Delegate Logger delegate) {  // Logger doesn't implement PaymentService
        this.delegate = delegate;
    }
    
    @Override
    public void processPayment(double amount) {
        // This won't work
    }
}

/**
 * Valid: Delegate type on method parameter (PaymentService) matches the decorated type
 * Should NOT trigger diagnostic
 */
@Decorator
@Dependent
class ValidDelegateTypeOnMethod implements PaymentService {
    
    private PaymentService delegate;
    
    @Inject
    public void setDelegate(@Delegate PaymentService delegate) {  // PaymentService implements PaymentService
        this.delegate = delegate;
    }
    
    @Override
    public void processPayment(double amount) {
        delegate.processPayment(amount);
    }
}

// Helper class for testing
class PaymentServiceImpl implements PaymentService {
    @Override
    public void processPayment(double amount) {
        // Implementation
    }
}