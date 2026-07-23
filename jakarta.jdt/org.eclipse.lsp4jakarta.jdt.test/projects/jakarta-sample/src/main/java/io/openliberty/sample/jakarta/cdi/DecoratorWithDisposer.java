package io.openliberty.sample.jakarta.cdi;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Disposes;

interface DecoratorService {
    void execute();
}

class DecoratorResource {
    public void cleanup() {}
}

@Decorator
public abstract class DecoratorWithDisposer implements DecoratorService {
    
    @Inject
    @Delegate
    private DecoratorService delegate;
    
    public void dispose(@Disposes DecoratorResource resource) {
        resource.cleanup();
    }
}
