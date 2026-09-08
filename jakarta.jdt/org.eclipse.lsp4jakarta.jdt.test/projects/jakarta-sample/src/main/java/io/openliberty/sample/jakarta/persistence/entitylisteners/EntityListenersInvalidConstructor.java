package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;

@Entity
@EntityListeners({ ProtectedConstructorListener.class, ParameterizedConstructorListener.class }) 
public class EntityListenersInvalidConstructor {

    @Id
    private int id;
}
