package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

/**
 * Sample class used to test CDI raw Event type diagnostic.
 *
 * Invalid cases: @Inject field or method parameter of raw type Event (no type argument).
 * Valid cases: @Inject field or method parameter using parameterized Event<T>.
 */
@ApplicationScoped
public class RawEventInjectionPoint {

    // Invalid: raw Event type — no type parameter
    @Inject
    Event rawEvent;

    // Valid: parameterized Event type
    @Inject
    Event<String> typedEvent;

    // Valid: another parameterized Event type
    @Inject
    Event<OrderCreated> orderEvent;

    // Valid: Event field with no @Inject — must NOT be flagged
    Event notInjectedRawEvent;

    // Invalid: @Inject method parameter of raw Event type
    @Inject
    public void setRawEvent(Event event) {
    }

    // Valid: @Inject method parameter of parameterized Event type
    @Inject
    public void setTypedEvent(Event<String> event) {
    }

    // Valid: @Inject method with mixed parameters — only raw Event param triggers diagnostic on method
    @Inject
    public void setMixed(String name, Event rawEvent) {
    }

    // Invalid: @Inject method with multiple raw Event params — one diagnostic per method (not per param)
    @Inject
    public void setMultipleRawEvents(Event first, Event second) {
    }

    // Valid: method with raw Event return type — not an injection point, must NOT be flagged
    public Event produceRawEvent() {
        return null;
    }

    // Invalid: nested class with raw Event injection point
    static class Inner {

        @Inject
        Event rawEventInInner;
    }

    /**
     * Simple placeholder class representing a domain event.
     */
    static class OrderCreated {
    }
}
