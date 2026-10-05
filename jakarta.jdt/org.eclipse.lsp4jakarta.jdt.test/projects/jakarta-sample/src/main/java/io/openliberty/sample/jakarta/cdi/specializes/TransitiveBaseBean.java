package io.openliberty.sample.jakarta.cdi.specializes;

import jakarta.enterprise.context.ApplicationScoped;

/** Base bean for the transitive test. TransitiveSpecializerA and TransitiveSpecializerB both ultimately specialize this. */
@ApplicationScoped
public class TransitiveBaseBean {

    public void execute() {
        System.out.println("Transitive base execution");
    }
}
