package io.openliberty.sample.jakarta.cdi.specializes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/** Invalid: transitively specializes TransitiveBaseBean via TransitiveSpecializerA. */
@Specializes
@ApplicationScoped
public class TransitiveSpecializerB extends TransitiveSpecializerA {

    @Override
    public void execute() {
        System.out.println("Transitive specializer B execution");
    }
}
