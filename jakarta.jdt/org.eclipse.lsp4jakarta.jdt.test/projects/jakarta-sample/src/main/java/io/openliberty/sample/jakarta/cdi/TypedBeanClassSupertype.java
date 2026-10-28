package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;

/**
 * Valid: @Typed specifies TypedBeanBase, which is a superclass of this bean and
 * therefore part of its unrestricted bean types.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
@ApplicationScoped
@Typed(TypedBeanBase.class)
public class TypedBeanClassSupertype extends TypedBeanBase implements TypedShop<String> {

    @Override
    public String sell() {
        return "item";
    }
}
