package io.openliberty.sample.jakarta.persistence.idclass;

import java.io.Serializable;

// This composite key class intentionally lacks @Embeddable to trigger the diagnostic
public class OrderIdMissingEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long customerId;
    private Long orderNumber;
}
