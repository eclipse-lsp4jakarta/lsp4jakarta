package io.openliberty.sample.jakarta.persistence.idclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(io.openliberty.sample.jakarta.persistence.idclass.OrderIdWithEmbeddable.class) // ✅ OrderIdWithEmbeddable has @Embeddable (FQ name)
public class OrderWithFQValidIdClass {

    @Id
    private Long customerId;

    @Id
    private Long orderNumber;
}
