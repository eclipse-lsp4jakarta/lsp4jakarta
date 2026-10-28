package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;

@Entity
@EntityListeners({ AbstractListener.class, PackagePrivateConstructorListener.class })
public class EntityListenersAbstractAndPackagePrivateConstructor {

    @Id
    private int id;
}
