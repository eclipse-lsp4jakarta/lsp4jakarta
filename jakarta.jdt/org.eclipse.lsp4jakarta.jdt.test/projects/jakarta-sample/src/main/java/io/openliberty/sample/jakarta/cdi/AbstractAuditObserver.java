package io.openliberty.sample.jakarta.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.ObserverMethod;

/**
 * An abstract class that implements ObserverMethod without overriding notify —
 * diagnostic must NOT fire because abstract classes may legally defer the
 * implementation to concrete subclasses.
 */
public abstract class AbstractAuditObserver implements ObserverMethod<AuditEvent> {

    @Override
    public Class<?> getBeanClass() {
        return getClass();
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
}
