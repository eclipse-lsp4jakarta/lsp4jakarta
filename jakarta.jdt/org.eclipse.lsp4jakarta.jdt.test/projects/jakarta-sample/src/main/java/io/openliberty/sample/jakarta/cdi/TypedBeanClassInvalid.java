package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;

/**
 * Invalid: @Typed specifies String.class which is not in the unrestricted bean
 * type set of this class (TypedBeanClassInvalid, TypedBeanBase, TypedShop, Object).
 *
 * <p>The container must detect this and treat it as a definition error.</p>
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
@ApplicationScoped
@Typed(String.class)
public class TypedBeanClassInvalid extends TypedBeanBase implements TypedShop<String> {

    @Override
    public String sell() {
        return "item";
    }
}
