package io.openliberty.sample.jakarta.cdi.specializes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/** Valid: sole specializer of ValidBaseBean — no diagnostic expected. */
@Specializes
@ApplicationScoped
public class ValidSpecializer extends ValidBaseBean {

    @Override
    public void process() {
        System.out.println("Valid specialization");
    }
}
