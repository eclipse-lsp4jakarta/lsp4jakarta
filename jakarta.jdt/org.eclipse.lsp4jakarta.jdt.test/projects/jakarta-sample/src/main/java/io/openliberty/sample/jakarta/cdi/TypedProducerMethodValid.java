package io.openliberty.sample.jakarta.cdi;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;

/**
 * Valid: @Typed on a producer method specifies List.class, which is in the
 * unrestricted bean types of the return type List&lt;String&gt;.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
public class TypedProducerMethodValid {

    @Produces
    @Dependent
    @Typed(List.class)
    public List<String> produceList() {
        return new ArrayList<>();
    }
}
