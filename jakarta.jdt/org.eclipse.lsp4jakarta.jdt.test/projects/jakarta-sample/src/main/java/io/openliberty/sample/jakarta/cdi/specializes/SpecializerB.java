package io.openliberty.sample.jakarta.cdi.specializes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/** Invalid: two beans (this class and SpecializerA) both specialize BaseBean. */
@Specializes
@ApplicationScoped
public class SpecializerB extends BaseBean {

    @Override
    public void execute() {
        System.out.println("Specializer B execution");
    }
}
