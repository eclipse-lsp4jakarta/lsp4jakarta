package io.openliberty.sample.jakarta.persistence.idclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(io.openliberty.sample.jakarta.persistence.idclass.OrderIdMissingEmbeddable.class) // ❌ OrderIdMissingEmbeddable lacks @Embeddable (FQ name)
public class OrderWithFQInvalidIdClass {

    @Id
    private Long customerId;

    @Id
    private Long orderNumber;
}
