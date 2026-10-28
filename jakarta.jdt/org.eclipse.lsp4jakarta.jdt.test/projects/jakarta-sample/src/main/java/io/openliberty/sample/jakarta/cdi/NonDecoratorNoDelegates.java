package io.openliberty.sample.jakarta.cdi;

import io.openliberty.sample.jakarta.cdi.decorator.PaymentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Valid: non-decorator class with @Inject field but NO @Delegate
// Should NOT trigger any diagnostic for InvalidDelegateOutsideDecorator
@ApplicationScoped
public class NonDecoratorNoDelegates {

    @Inject
    private PaymentService service;

}
