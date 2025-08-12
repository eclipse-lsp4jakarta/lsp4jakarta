package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

@ApplicationScoped
public class ProducesAndDisposesObservesObservesAsync {
    @Produces
    public String greetDisposes(@Disposes String name) {
        return "Hi " + name + "!";
    }
    
    
    @Produces
    public String greetObserves(@Observes String name) {
        return "Hi " + name + "!";
    }
    
    
    @Produces
    public String greetObservesAsync(@ObservesAsync String name) {
        return "Hi " + name + "!";
    }
    
    
    @Produces
    public String greetDisposesObserves(@Disposes String name1, @Observes String name2) {
        return "Hi " + name1 + " and " + name2 + "!";
    }
    
    
    @Produces
    public String greetObservesObservesAsync(@Observes String name1, @ObservesAsync String name2) {
        return "Hi " + name1 + " and " + name2 + "!";
    }
    
    
    @Produces
    public String greetDisposesObservesAsync(@Disposes String name1, @ObservesAsync String name2) {
        return "Hi " + name1 + " and " + name2 + "!";
    }
    
    
    @Produces
    public String greetDisposesObservesObservesAsync(@Disposes String name1, @Observes String name2, @ObservesAsync String name3) {
        return "Hi " + name1 + ", " + name2 + " and " + name3 + "!";
    }
    
    
    @Produces
    public String greetDisposesObservesObservesAsync2(@Disposes @Observes @ObservesAsync String name) {
        return "Hi " + name + "!";
    }
}
