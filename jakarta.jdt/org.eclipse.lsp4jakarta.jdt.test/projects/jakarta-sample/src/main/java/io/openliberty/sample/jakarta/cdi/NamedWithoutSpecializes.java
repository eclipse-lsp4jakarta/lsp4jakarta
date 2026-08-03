package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Valid: A regular (non-specialized) bean that declares @Named.
 * @Named is perfectly legal on a non-specialized bean — no diagnostic expected.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §4.3</a>
 */
@Named("defaultService")
@ApplicationScoped
public class NamedWithoutSpecializes {

    public String greet() {
        return "Hello from NamedWithoutSpecializes";
    }
}
