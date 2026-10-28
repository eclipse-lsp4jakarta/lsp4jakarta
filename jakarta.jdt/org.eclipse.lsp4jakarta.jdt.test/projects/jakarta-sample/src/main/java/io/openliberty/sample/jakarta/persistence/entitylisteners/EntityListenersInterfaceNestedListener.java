package io.openliberty.sample.jakarta.persistence.entitylisteners;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;

@Entity
@EntityListeners({ InterfaceHolderWithListener.ListenerInsideInterface.class })
public class EntityListenersInterfaceNestedListener {

    @Id
    private int id;
}
