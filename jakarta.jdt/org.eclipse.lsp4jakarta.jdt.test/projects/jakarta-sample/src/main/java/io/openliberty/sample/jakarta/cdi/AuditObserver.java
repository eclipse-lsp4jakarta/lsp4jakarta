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
 * Invalid CDI definition: AuditObserver implements ObserverMethod but overrides
 * neither notify(T) nor notify(EventContext&lt;T&gt;).
 *
 * <p>The container will detect this as a definition error at deployment time because
 * neither notify overload is provided, so the observer logic cannot be invoked.
 */
public class AuditObserver implements ObserverMethod<AuditEvent> {

    // Invalid: does not override notify(T) or notify(EventContext<T>)

    @Override
    public Class<?> getBeanClass() {
        return AuditObserver.class;
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
