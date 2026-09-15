package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EmbeddedWithEmbeddableOnMethod {

    private Long id;
    private AddressWithEmbedded address;

    @Id
    public Long getId() {
        return id;
    }

    @Embedded
    public AddressWithEmbedded getAddress() {
        return address;
    }
}
