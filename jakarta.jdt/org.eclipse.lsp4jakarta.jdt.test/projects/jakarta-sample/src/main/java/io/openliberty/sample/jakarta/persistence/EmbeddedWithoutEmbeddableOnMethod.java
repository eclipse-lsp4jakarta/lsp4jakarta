package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EmbeddedWithoutEmbeddableOnMethod {

    private Long id;
    private EmbeddedAddress address;

    @Id
    public Long getId() {
        return id;
    }

    @Embedded
    public EmbeddedAddress getAddress() {
        return address;
    }
}
