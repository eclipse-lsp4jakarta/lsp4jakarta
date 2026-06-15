package io.openliberty.sample.jakarta.persistence.version;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class VersionValidInt {
    @Id
    private Long id;

    @Version
    private int version; // Valid - primitive int
}

