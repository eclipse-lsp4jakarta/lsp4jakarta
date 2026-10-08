package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EmbeddedWithoutEmbeddable {

    @Id
    private Long id;

    @Embedded
    private EmbeddedAddress address;  // ❌ EmbeddedAddress is not @Embeddable
}
