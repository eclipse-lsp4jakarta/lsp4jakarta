package io.openliberty.sample.jakarta.cdi;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;

/**
 * Invalid: @Typed on a producer method specifies String.class, which is NOT
 * in the unrestricted bean types of the return type List&lt;String&gt;
 * (which are: List, AbstractList, ..., Object).
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
public class TypedProducerMethodInvalid {

    @Produces
    @Dependent
    @Typed(String.class)
    public List<String> produceList() {
        return new ArrayList<>();
    }
}
