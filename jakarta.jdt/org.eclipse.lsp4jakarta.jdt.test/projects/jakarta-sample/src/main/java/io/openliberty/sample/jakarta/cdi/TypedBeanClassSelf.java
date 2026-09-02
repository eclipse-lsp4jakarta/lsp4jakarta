package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;

/**
 * Valid: @Typed specifies the bean class itself (TypedBeanClassSelf), which is
 * always part of its own unrestricted bean types.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
@ApplicationScoped
@Typed(TypedBeanClassSelf.class)
public class TypedBeanClassSelf extends TypedBeanBase implements TypedShop<String> {

    @Override
    public String sell() {
        return "item";
    }
}
