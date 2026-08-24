package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;

/**
 * Valid: @Typed on a producer method whose return type is a type variable (T).
 * When the return type cannot be resolved to a concrete class, no diagnostic
 * should be reported. This covers the "raw type variable" edge case.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
public class TypedProducerMethodGenericReturnType<T> {

    @Produces
    @Dependent
    @Typed(Object.class)
    public T produce() {
        return null;
    }
}
