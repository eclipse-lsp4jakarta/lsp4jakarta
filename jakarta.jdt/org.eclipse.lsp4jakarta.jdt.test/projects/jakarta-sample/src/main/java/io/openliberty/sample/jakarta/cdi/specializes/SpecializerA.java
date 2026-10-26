package io.openliberty.sample.jakarta.cdi.specializes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/** Invalid: two beans (this class and SpecializerB) both specialize BaseBean. */
@Specializes
@ApplicationScoped
public class SpecializerA extends BaseBean {

    @Override
    public void execute() {
        System.out.println("Specializer A execution");
    }
}
