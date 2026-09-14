package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Named;

/**
 * Invalid: Specialized bean that declares a bare @Named annotation (no value).
 * Per CDI 3.0 spec section 4.3, a specialized bean must not declare any @Named
 * annotation — with or without an explicit value. The name is inherited from the
 * bean it specializes.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §4.3</a>
 */
@Specializes
@Named
@ApplicationScoped
public class SpecializedBeanWithBareName {

    public String greet() {
        return "Hello from SpecializedBeanWithBareName";
    }
}
