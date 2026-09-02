package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EmbeddedWithEmbeddable {

    @Id
    private Long id;

    @Embedded
    private AddressWithEmbedded address;  // ✅ AddressWithEmbedded is annotated with @Embeddable
}
