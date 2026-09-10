package io.openliberty.sample.jakarta.cdi;

import io.openliberty.sample.jakarta.cdi.decorator.PaymentService;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Invalid: @Delegate used outside a decorator class (class is not annotated with @Decorator)
@ApplicationScoped
public class NotADecorator {

    // Invalid: @Delegate on a field in a non-decorator class
    @Inject
    @Delegate
    private PaymentService service;

}
