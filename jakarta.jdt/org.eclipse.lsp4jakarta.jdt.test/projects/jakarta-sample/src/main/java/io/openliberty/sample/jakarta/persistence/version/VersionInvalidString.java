package io.openliberty.sample.jakarta.persistence.version;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class VersionInvalidString {
    @Id
    private Long id;

    @Version
    private String version; // Invalid - String not supported
}
