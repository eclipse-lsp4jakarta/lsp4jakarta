package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;

@Entity
@EntityListeners({ OuterListenerContainer.StaticNestedImplicitListener.class, OuterListenerContainer.StaticNestedExplicitListener.class })
public class EntityListenersStaticNestedConstructor {

    @Id
    private int id;
}
