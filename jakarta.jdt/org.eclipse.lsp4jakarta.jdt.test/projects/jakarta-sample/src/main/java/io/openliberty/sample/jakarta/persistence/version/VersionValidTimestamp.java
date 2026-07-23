package io.openliberty.sample.jakarta.persistence.version;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.sql.Timestamp;

@Entity
public class VersionValidTimestamp {
    @Id
    private Long id;

    @Version
    private Timestamp version; // Valid - java.sql.Timestamp
}

