package io.openliberty.sample.jakarta.cdi.specializes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/** Directly specializes TransitiveBaseBean. Superclass of TransitiveSpecializerB. */
@Specializes
@ApplicationScoped
public class TransitiveSpecializerA extends TransitiveBaseBean {

    @Override
    public void execute() {
        System.out.println("Transitive specializer A execution");
    }
}
