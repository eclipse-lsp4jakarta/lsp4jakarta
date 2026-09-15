package io.openliberty.sample.jakarta.persistence.version;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class VersionMethodValidLong {
    @Id
    private Long id;

    private Long version;

    @Version
    public Long getVersion() { // Valid - Long is supported
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

