package io.openliberty.sample.jakarta.persistence.version;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class VersionMethodInvalidString {
    @Id
    private Long id;

    private String version;

    @Version
    public String getVersion() { // Invalid - String not supported
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

