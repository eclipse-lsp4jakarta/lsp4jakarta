package io.openliberty.sample.jakarta.cdi;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;

/**
 * Valid: @Typed(List.class) on a producer method returning List&lt;T&gt;.
 * The method is @Dependent (satisfying the CDI §3.3 scope rule for producers
 * with a type-variable type argument). The @Typed value (List) is in the
 * unrestricted bean-type set of java.util.List, so no @Typed diagnostic fires.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
public class TypedProducerMethodGenericReturnType<T> {

    @Produces
    @Dependent
    @Typed(List.class)
    public List<T> produce() {
        return null;
    }
}
