package io.openliberty.sample.jakarta.persistence.idclass;

import java.io.Serializable;
import jakarta.persistence.Embeddable;

// Correctly annotated with @Embeddable — used to verify the valid case
@Embeddable
public class OrderIdWithEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long customerId;
    private Long orderNumber;
}
