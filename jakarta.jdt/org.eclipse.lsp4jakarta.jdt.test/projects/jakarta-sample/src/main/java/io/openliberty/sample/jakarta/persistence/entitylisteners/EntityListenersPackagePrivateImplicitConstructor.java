package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;

@Entity
@EntityListeners(PackagePrivateImplicitConstructorListener.class)
public class EntityListenersPackagePrivateImplicitConstructor {

    @Id
    private int id;
}
