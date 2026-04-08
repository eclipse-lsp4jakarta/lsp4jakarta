package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.event.Reception;

// Test case 1: @Dependent with conditional @Observes (should trigger diagnostic)
@Dependent
public class DependentScopedConditionalObserver {
    
    public void observerMethod(@Observes(notifyObserver = Reception.IF_EXISTS) String event) {
        // This should trigger a diagnostic
    }
}

// Test case 2: @Dependent with conditional @ObservesAsync (should trigger diagnostic)
@Dependent
class DependentScopedConditionalObserverAsync {
    
    public void observerMethod(@ObservesAsync(notifyObserver = Reception.IF_EXISTS) String event) {
        // This should trigger a diagnostic
    }
}

// Test case 3: @Dependent with ALWAYS (should NOT trigger diagnostic)
@Dependent
class DependentScopedAlwaysObserver {
    
    public void observerMethod(@Observes(notifyObserver = jakarta.enterprise.event.Reception.ALWAYS) String event) {
        // This should NOT trigger a diagnostic
    }
}

// Test case 4: @Dependent without notifyObserver attribute (should NOT trigger diagnostic - defaults to ALWAYS)
@Dependent
class DependentScopedDefaultObserver {
    
    public void observerMethod(@Observes String event) {
        // This should NOT trigger a diagnostic (defaults to ALWAYS)
    }
}

// Test case 5: @ApplicationScoped with conditional observer (should NOT trigger diagnostic)
@ApplicationScoped
class ApplicationScopedConditionalObserver {
    
    public void observerMethod(@Observes(notifyObserver = Reception.IF_EXISTS) String event) {
        // This should NOT trigger a diagnostic (not @Dependent)
    }
}
