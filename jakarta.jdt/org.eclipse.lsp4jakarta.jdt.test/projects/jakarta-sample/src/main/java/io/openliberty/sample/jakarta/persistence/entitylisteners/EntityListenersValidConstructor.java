package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;

@Entity
@EntityListeners({ ExplicitPublicConstructorListener.class, ImplicitDefaultConstructorListener.class })
public class EntityListenersValidConstructor {

    @Id
    private int id;
}
