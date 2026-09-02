package io.openliberty.sample.jakarta.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.ObserverMethod;

/**
 * Invalid CDI definition: implements the raw type {@code ObserverMethod} (no type
 * argument) and provides no notify override.
 *
 * <p>Tests that the diagnostic fires correctly even for the raw-type case, and that
 * the quick-fix labels fall back to {@code Object} when no type argument is present.
 */
@SuppressWarnings("rawtypes")
public class AuditObserverRawType implements ObserverMethod {

    // Invalid: raw ObserverMethod with no notify override

    @Override
    public Class<?> getBeanClass() {
        return AuditObserverRawType.class;
    }

    @Override
    public Type getObservedType() {
        return Object.class;
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
