package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Valid: Specialized bean that does NOT declare an explicit bean name.
 * The bean name is inherited from the bean it specializes — no diagnostic expected.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §4.3</a>
 */
@Specializes
@ApplicationScoped
public class ValidSpecializedBean {

    public String greet() {
        return "Hello from ValidSpecializedBean";
    }
}
