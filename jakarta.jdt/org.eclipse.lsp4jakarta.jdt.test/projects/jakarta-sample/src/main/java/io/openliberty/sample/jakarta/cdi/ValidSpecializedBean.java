package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Valid: Specialized bean that does NOT declare an explicit bean name.
 * The bean name is inherited from the bean it specializes — no diagnostic expected.
 */
@Specializes
@ApplicationScoped
public class ValidSpecializedBean extends BaseServiceWithScopeForValid {

    public String greet() {
        return "Hello from ValidSpecializedBean";
    }
}
