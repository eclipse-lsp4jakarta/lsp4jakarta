package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;

@Entity
@EntityListeners({ OuterListenerContainer.NonStaticInnerImplicitListener.class, OuterListenerContainer.NonStaticInnerExplicitListener.class })
public class EntityListenersInnerClassConstructor {

    @Id
    private int id;
}
