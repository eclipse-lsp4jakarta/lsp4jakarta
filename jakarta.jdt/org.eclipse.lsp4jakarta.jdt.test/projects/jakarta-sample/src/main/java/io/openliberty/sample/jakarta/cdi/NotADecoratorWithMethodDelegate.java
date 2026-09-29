package io.openliberty.sample.jakarta.cdi;

import io.openliberty.sample.jakarta.cdi.decorator.PaymentService;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Invalid: @Delegate used on method/constructor parameters outside a decorator class
@ApplicationScoped
public class NotADecoratorWithMethodDelegate {

    // Invalid: @Delegate on an initializer method parameter in a non-decorator class
    @Inject
    public void init(@Delegate PaymentService service) {
    }

    // Invalid: @Delegate on a constructor parameter in a non-decorator class
    @Inject
    public NotADecoratorWithMethodDelegate(@Delegate PaymentService ps) {
    }

}
