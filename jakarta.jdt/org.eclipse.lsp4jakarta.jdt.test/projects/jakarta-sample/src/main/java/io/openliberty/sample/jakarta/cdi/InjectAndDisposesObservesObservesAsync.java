package io.openliberty.sample.jakarta.cdi;

import jakarta.inject.Inject;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

public class InjectAndDisposesObservesObservesAsync {
    
    @Inject
    public String greetDisposes(@jakarta.enterprise.inject.Disposes String name) {
        return "Hi " + name + "!";
    }
    
    
    @Inject
    public String greetObserves(@Observes String name) {
        return "Hi " + name + "!";
    }
    
    
    @Inject
    public String greetObservesAsync(@ObservesAsync String name) {
        return "Hi " + name + "!";
    }
    
    
    @Inject
    public String greetDisposesObserves(@Disposes String name1, @Observes String name2) {
        return "Hi " + name1 + " and " + name2 + "!";
    }
    
    
    @Inject
    public String greetObservesObservesAsync(@Observes String name1, @jakarta.enterprise.event.ObservesAsync String name2) {
        return "Hi " + name1 + " and " + name2 + "!";
    }
    
    
    @Inject
    public String greetDisposesObservesAsync(@Disposes String name1, @ObservesAsync String name2) {
        return "Hi " + name1 + " and " + name2 + "!";
    }
    
    
    @Inject
    public String greetDisposesObservesObservesAsync(@Disposes String name1, @Observes String name2, @ObservesAsync String name3) {
        return "Hi " + name1 + ", " + name2 + " and " + name3 + "!";
    }
    
    @Inject
    public String greetDisposesObservesObservesAsync2(@Disposes @Observes @ObservesAsync String name) {
        return "Hi " + name + "!";
    }
}
