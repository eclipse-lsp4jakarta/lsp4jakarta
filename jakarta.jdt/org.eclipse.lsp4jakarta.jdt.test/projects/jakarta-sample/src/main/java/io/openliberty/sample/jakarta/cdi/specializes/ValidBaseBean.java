package io.openliberty.sample.jakarta.cdi.specializes;

import jakarta.enterprise.context.ApplicationScoped;

/** Base bean. Only ValidSpecializer extends this — no conflict. */
@ApplicationScoped
public class ValidBaseBean {

    public void process() {
        System.out.println("Valid base execution");
    }
}
