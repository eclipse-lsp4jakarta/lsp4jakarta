package io.openliberty.sample.jakarta.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.EventContext;
import jakarta.enterprise.inject.spi.ObserverMethod;

/**
 * Valid CDI definition: overrides notify(EventContext&lt;T&gt;), so the container
 * can invoke the observer logic via the EventContext overload.
 */
public class AuditObserverWithEventContext implements ObserverMethod<AuditEvent> {

    @Override
    public Class<?> getBeanClass() {
        return AuditObserverWithEventContext.class;
    }

    @Override
    public Type getObservedType() {
        return AuditEvent.class;
    }

    @Override
    public Set<Annotation> getObservedQualifiers() {
        return Collections.emptySet();
    }

    @Override
    public Reception getReception() {
        return Reception.ALWAYS;
    }

    @Override
    public TransactionPhase getTransactionPhase() {
        return TransactionPhase.IN_PROGRESS;
    }

    @Override
    public void notify(EventContext<AuditEvent> eventContext) {
        System.out.println("Audit event context received: " + eventContext.getEvent());
    }
}
