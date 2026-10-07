package io.openliberty.sample.jakarta.cdi;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;

/**
 * Valid: @Typed specifies only types that are in the unrestricted set of bean
 * types (the bean class itself, its superclasses, and its interfaces).
 *
 * <p>Bean class hierarchy:
 * <ul>
 * <li>TypedBeanClassValid extends TypedBeanBase implements TypedShop&lt;String&gt;</li>
 * </ul>
 * Unrestricted bean types: TypedBeanClassValid, TypedBeanBase, TypedShop, Object
 * </p>
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
@ApplicationScoped
@Typed(TypedShop.class)
public class TypedBeanClassValid extends TypedBeanBase implements TypedShop<String> {

    @Override
    public String sell() {
        return "item";
    }
}
