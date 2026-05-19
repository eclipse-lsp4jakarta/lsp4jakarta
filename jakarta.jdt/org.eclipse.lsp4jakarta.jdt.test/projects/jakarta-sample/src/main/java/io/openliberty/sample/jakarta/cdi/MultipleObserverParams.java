package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

public class MultipleObserverParams {
    
    public void validSingleObserves(@Observes String event) {
        // Valid: Method with a single @Observes parameter
    }
    
    public void validSingleObservesAsync(@ObservesAsync String event) {
        // Valid: Method with a single @ObservesAsync parameter
    }

    public void invalidTwoObserves(@Observes String event1, @Observes String event2) {
        // Invalid: Method cannot have multiple parameters with @Observes annotations
    }

    public void invalidObservesAndObservesAsync(@Observes String event1, @ObservesAsync String event2) {
        // Invalid: Method cannot have parameters with both @Observes and @ObservesAsync annotations
    }

    public void invalidThreeObserves(@Observes String event1, @ObservesAsync String event2, @ObservesAsync String event3) {
        // Invalid: Method cannot have more than one parameter with observer annotations (@Observes or @ObservesAsync)
    }
}

