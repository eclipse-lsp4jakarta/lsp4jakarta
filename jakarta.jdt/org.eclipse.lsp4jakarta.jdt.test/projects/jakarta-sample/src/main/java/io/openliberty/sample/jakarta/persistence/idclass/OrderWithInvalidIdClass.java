package io.openliberty.sample.jakarta.persistence.idclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(OrderIdMissingEmbeddable.class) // ❌ OrderIdMissingEmbeddable lacks @Embeddable
public class OrderWithInvalidIdClass {

    @Id
    private Long customerId;

    @Id
    private Long orderNumber;
}
