package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;

/**
 * Invalid: @Typed specifies two values — TypedShop (valid, it is implemented) and
 * String (invalid, not in the unrestricted bean types). Only String should produce
 * a diagnostic; TypedShop should not.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
@ApplicationScoped
@Typed({ TypedShop.class, String.class })
public class TypedBeanClassMultipleValues extends TypedBeanBase implements TypedShop<String> {

    @Override
    public String sell() {
        return "item";
    }
}
