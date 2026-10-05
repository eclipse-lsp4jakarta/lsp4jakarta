package io.openliberty.sample.jakarta.cdi.specializes;

import jakarta.enterprise.context.ApplicationScoped;

/** Base bean. SpecializerA and SpecializerB both specialize this — inconsistent specialization. */
@ApplicationScoped
public class BaseBean {

    public void execute() {
        System.out.println("Base execution");
    }
}
